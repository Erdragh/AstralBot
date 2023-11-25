package dev.erdragh.erdbot

import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.util.UUID

object WhitelistHandler {
    // See: https://github.com/JetBrains/Exposed/wiki/
    private val db = Database.connect("jdbc:sqlite:whitelist.db", "org.sqlite.JDBC")

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
        var result: UUID? = null;
        transaction {
            val query = WhitelistedUser.select { WhitelistedUser.discordID eq discordID }
            result = if (query.empty()) null else query.iterator().next()[WhitelistedUser.minecraftID]
        }
        return result
    }
}