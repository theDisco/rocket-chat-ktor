package com.example.config

import com.example.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileInputStream
import java.util.*

object Database {
    private val properties = Properties().apply {
        load(FileInputStream("src/main/resources/db.properties"))
    }

    fun connect() {
        val hikariPool = hikariDataSource()
        Database.connect(hikariPool)
    }

    fun migrate() {
        transaction {
            SchemaUtils.create(Conversations, MessageStatuses, Messages, Participants, Users)
        }
    }

    private fun hikariDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            properties.getProperty("database.jdbcUrl")?.let { jdbcUrl = it }
            properties.getProperty("database.driverClassName")?.let { driverClassName = it }
            properties.getProperty("database.username")?.let { username = it }
            properties.getProperty("database.password")?.let { password = it }
            properties.getProperty("database.maximumPoolSize")?.let { maximumPoolSize = it.toInt() }
            validate()
        }
        return HikariDataSource(config)
    }

    suspend fun <T> runQuery(block: () -> T): T {
        return withContext(Dispatchers.IO) {
            transaction { block() }
        }
    }
}
