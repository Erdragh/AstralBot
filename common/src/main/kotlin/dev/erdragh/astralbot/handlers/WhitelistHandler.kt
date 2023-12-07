package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import dev.erdragh.astralbot.baseDirectory
import dev.erdragh.astralbot.config.AstralBotConfig
import net.dv8tion.jda.api.entities.User
import net.minecraft.network.chat.Component
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection
import java.util.*
import java.util.Random
import kotlin.collections.ArrayList
import kotlin.random.asKotlinRandom
import kotlin.random.nextInt

/**
 * This is a Handler for the whitelisting process for AstralBot.
 *
 * It communicates with an SQLite database in the astralbot folder
 * using [JetBrains Exposed](https://github.com/JetBrains/Exposed).
 *
 * It's also responsible for the message shown to the User when
 * they try to log in but aren't whitelisted yet.
 * @author Erdragh
 */
object WhitelistHandler {
    // This is the database containing the whitelisting data.
    private val db = Database.connect(
        "jdbc:sqlite:${
            // The SQLite db is in the whitelist.db file in the
            // astralbot directory in the main server directory
            File(baseDirectory, "whitelist.db").absolutePath
        }", "org.sqlite.JDBC"
    )

    // Stores the login codes in Memory associated with the
    // Users whose code it is. Storing these in memory only
    // Results in users getting a new code when the server
    // restarts, which is acceptable in my opinion.
    private val loginCodes = HashMap<Int, UUID>()
    // The random used to generate the login codes.
    // I'm using .asKotlinRandom() here because the
    // default Kotlin Random constructor wants a seed.
    private val loginRandom = Random().asKotlinRandom()
    // This is the file containing the message template
    // for when a User isn't whitelisted yet.
    private val whitelistTemplate: File

    /*
     * This is the Kotlin representation of the contents of
     * the whitelisting database. This is really elegant because
     * Exposed in itself is very elegant.
     */
    private object WhitelistedUser : Table() {
        val discordID: Column<Long> = long("discordID")
        val minecraftID: Column<UUID> = uuid("minecraftID")
        // Overwriting the primary key like this prevents linking
        // multiple times. This is prevented elsewhere too, but
        // redundancy is not a bad thing.
        override val primaryKey = PrimaryKey(arrayOf(discordID, minecraftID), name = "PK_Whitelisted_ID")
    }

    init {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        TransactionManager.defaultDatabase = db
        transaction {
            // Creates the Table in the database file if it didn't exist beforehand.
            // Exposed will create the SQLite file if none is present.
            if (SchemaUtils.listTables().find { it == WhitelistedUser.javaClass.simpleName } == null) {
                SchemaUtils.create(WhitelistedUser)
            }
        }

        // Makes sure the whitelist template actually exists and prefills
        // it with a generic template
        whitelistTemplate = File(baseDirectory, "whitelist.txt")
        if (whitelistTemplate.createNewFile()) {
            whitelistTemplate.writeText(
                """
                Hello {{USER}}, you're not whitelisted yet.
                
                To whitelist yourself, join the discord:
                {{DISCORD}}
                
                and run the /link command with the following code:
                
                {{CODE}}
            """.trimIndent()
            )
        }
    }

    /**
     * Returns a list of all Discord userIDs that are
     * contained in the database, i.e. are whitelisted.
     * @return list of whitelisted discord users
     */
    fun getAllUsers(): Collection<Long> {
        var result: ArrayList<Long>? = null
        transaction {
            val query = WhitelistedUser.selectAll()
            val r = ArrayList<Long>(query.count().toInt())
            query.iterator().forEach {
                r.add(it[WhitelistedUser.discordID])
            }
            result = r
        }
        return result ?: throw RuntimeException("Failed to get whitelisted users")
    }

    /**
     * Whitelists a Discord User + Minecraft User combo in the
     * Database. Also removes the User's link code from Memory.
     * @param user the Discord user who wants to get whitelisted
     * @param id the Minecraft user ID of the account to get linked
     */
    fun whitelist(user: User, id: UUID) {
        transaction {
            WhitelistedUser.insert {
                it[discordID] = user.idLong
                it[minecraftID] = id
            }
        }
        loginCodes.remove(getWhitelistCode(id))
    }

    /**
     * Removes a User from the whitelist DB
     * @param user the Discord User whose linking status
     * will be removed
     */
    fun unWhitelist(user: User) {
        transaction {
            WhitelistedUser.deleteWhere {
                discordID eq user.idLong
            }
        }
    }

    /**
     * Looks up whether the given Minecraft ID has a Whitelist entry.
     * @param minecraftID The Minecraft user ID which will be looked up
     * @return `null` if the given ID isn't whitelisted and the linked
     * Discord ID otherwise.
     */
    fun checkWhitelist(minecraftID: UUID): Long? {
        var result: Long? = null
        transaction {
            val query = WhitelistedUser.select { WhitelistedUser.minecraftID eq minecraftID }
            result = if (query.empty()) null else query.iterator().next()[WhitelistedUser.discordID]
        }
        return result
    }

    /**
     * Looks up whether the given Discord ID has a Whitelist entry.
     * @param discordID The Discord user ID which will be looked up
     * @return `null` if the given ID isn't whitelisted and the linked
     * Minecraft ID otherwise.
     */
    fun checkWhitelist(discordID: Long): UUID? {
        var result: UUID? = null
        transaction {
            val query = WhitelistedUser.select { WhitelistedUser.discordID eq discordID }
            result = if (query.empty()) null else query.iterator().next()[WhitelistedUser.minecraftID]
        }
        return result
    }

    /**
     * Checks whether a User is actually whitelisted and is allowed to join the server.
     * This method gets used in the PlayerList mixins to expand the isWhiteListed check.
     * @param minecraftID the Minecraft ID of the player wanting to join
     * @param defaultWhitelisted whether the player is whitelisted by other means, e.g.
     * being an Operator, being on the vanilla whitelist or the server having disabled
     * the vanilla whitelist.
     * @return whether the Minecraft User is allowed to log in based on the extended
     * whitelist check
     */
    fun handleLoginAttempt(minecraftID: UUID, defaultWhitelisted: Boolean): Boolean {
        // This value represents whether the user is whitelisted in the DB
        val isWhitelisted = checkWhitelist(minecraftID) != null
        val hasToBeWhitelistedByLink =
            (!isWhitelisted && AstralBotConfig.REQUIRE_LINK_FOR_WHITELIST.get()) || (!isWhitelisted && !defaultWhitelisted)
        // Generates a link code only if the user doesn't have one and has to go through linking to get one
        if (!loginCodes.containsValue(minecraftID) && hasToBeWhitelistedByLink) {
            val loginCodeRange = 10000..99999
            var whitelistCode = loginRandom.nextInt(loginCodeRange)
            // The following line could be vulnerable to a DOS attack
            // I accept the possibility of a login code possibly getting overwritten
            // so this DOS won't cause an infinite loop. Such a DOS may still cause
            // Players to not be able to whitelist.
            // while (loginCodes.containsKey(whitelistCode)) whitelistCode = loginRandom.nextInt(loginCodeRange)
            loginCodes[whitelistCode] = minecraftID
        }

        // The config option is false by default, which means other methods of whitelisting
        // still work like you'd expect them to. Enabling the config option *forces* everybody
        // who wants to play to link their account, even Operators and even if the vanilla
        // whitelist is turned off.
        return if (AstralBotConfig.REQUIRE_LINK_FOR_WHITELIST.get()) {
            isWhitelisted && defaultWhitelisted
        } else {
            isWhitelisted || defaultWhitelisted
        }
    }

    /**
     * Fetches the login code from [loginCodes]
     * @param minecraftID The user whose login code will be returned
     * @return the login code of the given user or `null` if there isn't
     * one for them yet.
     */
    fun getWhitelistCode(minecraftID: UUID): Int? {
        return loginCodes.entries.find { it.value == minecraftID }?.key
    }

    /**
     * Gets the Minecraft ID for the given Login Code
     * @param code the login code of which the associated
     * user ID will be returned
     * @return the Minecraft ID connected with the given
     * link code or `null` if nobody is associated with
     * the given code.
     */
    fun getPlayerFromCode(code: Int): UUID? {
        return loginCodes[code]
    }

    /**
     * Formats the whitelist message with the template in
     * [whitelistTemplate] by replacing the following:
     * - `{{USER}}` with the username of the given [user]
     * - `{{CODE}}` with the linkCode associated with the given [user]
     * - `{{DISCORD}}` with the link to the Discord server
     * configured in the [AstralBotConfig]
     */
    fun writeWhitelistMessage(user: GameProfile): Component {
        val template = whitelistTemplate.readText()
            .replace("{{USER}}", user.name)
            .replace("{{CODE}}", getWhitelistCode(user.id).toString())
            .split("{{DISCORD}}")

        // Throws an error if the template is malformed
        require(template.isNotEmpty() && template[0].isNotEmpty()) { "Discord Template empty" }

        val discordLink = AstralBotConfig.DISCORD_LINK.get()

        val component = Component.empty()
        for (i in template.indices) {
            component.append(template[i])
            if (discordLink.isNotEmpty() && i + 1 < template.size) {
                // TODO: Make this clickable. Using `withStyle` and a ClickEvent did not seem to work
                component.append(Component.literal(discordLink))
            }
        }
        return component
    }
}