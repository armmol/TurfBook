package com.sports.turfbook.database

import com.sports.turfbook.database.tables.OtpCodesTable
import com.sports.turfbook.database.tables.RefreshTokensTable
import com.sports.turfbook.database.tables.UserPreferredSportsTable
import com.sports.turfbook.database.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        Database.connect(createDataSource())
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                UserPreferredSportsTable,
                OtpCodesTable,
                RefreshTokensTable
            )
        }
    }

    private fun createDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            jdbcUrl = System.getenv("DATABASE_URL")
                ?: "jdbc:postgresql://localhost:5432/turfbook"
            username = System.getenv("DATABASE_USER") ?: "turfbook"
            password = System.getenv("DATABASE_PASSWORD") ?: "turfbook_dev"
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            minimumIdle = 2
            idleTimeout = 300_000
            connectionTimeout = 20_000
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }
}
