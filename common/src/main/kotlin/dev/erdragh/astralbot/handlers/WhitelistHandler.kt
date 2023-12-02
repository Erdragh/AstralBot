package dev.erdragh.astralbot.handlers

import com.mojang.authlib.GameProfile
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.*
import java.util.Random
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.random.asKotlinRandom
import kotlin.random.nextInt

object WhitelistHandler {
    // See: https://github.com/JetBrains/Exposed/wiki/
    private val db = Database.connect("jdbc:sqlite:whitelist.db", "org.sqlite.JDBC")

    private val loginCodes = HashMap<Int, UUID>()
    private val loginRandom = Random().asKotlinRandom()

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

    fun handleLoginAttempt(minecraftID: UUID): Boolean {
        val isWhitelisted = checkWhitelist(minecraftID) != null
        if (!loginCodes.containsValue(minecraftID)) {
            val loginCodeRange = 10000..99999
            var whitelistCode = loginRandom.nextInt(loginCodeRange)
            while (loginCodes.containsKey(whitelistCode)) whitelistCode = loginRandom.nextInt(loginCodeRange)
            loginCodes[whitelistCode] = minecraftID
        }
        return isWhitelisted
    }

    fun getWhitelistCode(minecraftID: UUID): Int? {
        return loginCodes.entries.find { it.value == minecraftID }?.key
    }

    fun getPlayerFromCode(code: Int): UUID? {
        return loginCodes[code]
    }
}