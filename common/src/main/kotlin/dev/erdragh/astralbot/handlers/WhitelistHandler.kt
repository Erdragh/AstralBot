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
import kotlin.random.asKotlinRandom
import kotlin.random.nextInt

object WhitelistHandler {
    // See: https://github.com/JetBrains/Exposed/wiki/
    private val db = Database.connect(
        "jdbc:sqlite:${
            File(baseDirectory, "whitelist.db").absolutePath
        }", "org.sqlite.JDBC"
    )

    private val loginCodes = HashMap<Int, UUID>()
    private val loginRandom = Random().asKotlinRandom()
    private val whitelistTemplate: File

    private object WhitelistedUser : Table() {
        val discordID: Column<Long> = long("discordID")
        val minecraftID: Column<UUID> = uuid("minecraftID")
        override val primaryKey = PrimaryKey(arrayOf(discordID, minecraftID), name = "PK_Whitelisted_ID")
    }

    init {
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        TransactionManager.defaultDatabase = db
        transaction {
            if (SchemaUtils.listTables().find { it == WhitelistedUser.javaClass.simpleName } == null) {
                SchemaUtils.create(WhitelistedUser)
            }
        }
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

    fun whitelist(user: User, id: UUID) {
        transaction {
            WhitelistedUser.insert {
                it[discordID] = user.idLong
                it[minecraftID] = id
            }
        }
        loginCodes.remove(getWhitelistCode(id))
    }

    fun unWhitelist(user: User) {
        transaction {
            WhitelistedUser.deleteWhere {
                discordID eq user.idLong
            }
        }
    }

    fun checkWhitelist(minecraftID: UUID): Long? {
        var result: Long? = null
        transaction {
            val query = WhitelistedUser.select { WhitelistedUser.minecraftID eq minecraftID }
            result = if (query.empty()) null else query.iterator().next()[WhitelistedUser.discordID]
        }
        return result
    }

    fun checkWhitelist(discordID: Long): UUID? {
        var result: UUID? = null
        transaction {
            val query = WhitelistedUser.select { WhitelistedUser.discordID eq discordID }
            result = if (query.empty()) null else query.iterator().next()[WhitelistedUser.minecraftID]
        }
        return result
    }

    fun handleLoginAttempt(minecraftID: UUID, defaultWhitelisted: Boolean): Boolean {
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
        return if (AstralBotConfig.REQUIRE_LINK_FOR_WHITELIST.get()) {
            isWhitelisted && defaultWhitelisted
        } else {
            isWhitelisted || defaultWhitelisted
        }
    }

    fun getWhitelistCode(minecraftID: UUID): Int? {
        return loginCodes.entries.find { it.value == minecraftID }?.key
    }

    fun getPlayerFromCode(code: Int): UUID? {
        return loginCodes[code]
    }

    fun writeWhitelistMessage(user: GameProfile): Component {
        val template = whitelistTemplate.readText()
            .replace("{{USER}}", user.name)
            .replace("{{CODE}}", getWhitelistCode(user.id).toString())
            .split("{{DISCORD}}")
        if (template.isEmpty() || template[0].isEmpty()) throw IllegalStateException("Whitelist template empty")
        val discordLink = AstralBotConfig.DISCORD_LINK.get()
        val component = Component.empty()
        for (i in template.indices) {
            component.append(template[i])
            if (discordLink.isNotEmpty() && i + 1 < template.size) {
                component.append(Component.literal(discordLink))
            }
        }
        return component
    }
}