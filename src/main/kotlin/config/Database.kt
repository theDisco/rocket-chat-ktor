package com.example.config

import com.example.*
import com.example.model.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.instrumentation.jdbc.datasource.JdbcTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileInputStream
import java.util.*
import javax.sql.DataSource

object Database {
    private val properties = Properties().apply {
        load(FileInputStream("src/main/resources/db.properties"))
    }

    fun connect(telemetry: OpenTelemetry) {
        val hikariPool = hikariDataSource(telemetry)
        Database.connect(hikariPool)
    }

    fun migrate() {
        transaction {
            SchemaUtils.create(Conversations, MessageStatuses, Messages, Participants, Users)
        }
    }

    private fun hikariDataSource(telemetry: OpenTelemetry): DataSource {
        val config = HikariConfig().apply {
            properties.getProperty("database.jdbcUrl")?.let { jdbcUrl = it }
            properties.getProperty("database.driverClassName")?.let { driverClassName = it }
            properties.getProperty("database.username")?.let { username = it }
            properties.getProperty("database.password")?.let { password = it }
            properties.getProperty("database.maximumPoolSize")?.let { maximumPoolSize = it.toInt() }

            minimumIdle = 5
            idleTimeout = 60000
            maxLifetime = 1800000
            connectionTimeout = 30000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            validate()
        }
        val dataSource = HikariDataSource(config)

        return JdbcTelemetry.create(telemetry).wrap(dataSource);
    }

    suspend fun <T> runQuery(block: suspend () -> T): T {
        val context = Context.current()
        return newSuspendedTransaction(Dispatchers.IO) {
            withContext(context.asContextElement()) {
                try {
                    val result = block()
                    commit()
                    result
                } catch (e: Exception) {
                    rollback()
                    throw e
                }
            }
        }
    }
}
