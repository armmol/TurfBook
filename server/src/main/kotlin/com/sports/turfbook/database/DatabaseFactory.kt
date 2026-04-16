package com.sports.turfbook.database

import com.sports.turfbook.database.tables.*
import com.sports.turfbook.domain.KnownSports
import com.sports.turfbook.domain.Sport
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

object DatabaseFactory {

    fun init() {
        Database.connect(createDataSource())
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                // Sports registry
                SportsTable,
                // Auth
                UsersTable,
                UserPreferredSportsTable,
                OtpCodesTable,
                RefreshTokensTable,
                // Turfs
                TurfsTable,
                TurfSportsTable,
                TurfAmenitiesTable,
                TurfPhotosTable,
                TurfSlotPricingTable,
                TurfExternalSystemTable,
                // Bookings & Payments
                BookingsTable,
                PaymentsTable,
                // Reviews
                ReviewsTable
            )
        }
        seedAndLoadSports()
    }

    /**
     * Seeds [SportsTable] with all [KnownSports] on first run (upsert — safe to repeat).
     * Then loads any admin-registered custom sports back into the in-memory registry.
     */
    private fun seedAndLoadSports() = transaction {
        // Upsert built-in sports
        KnownSports.all.forEach { sport ->
            SportsTable.upsert(SportsTable.code) {
                it[SportsTable.code] = sport.code
                it[SportsTable.displayName] = sport.displayName
                it[SportsTable.iconKey] = sport.iconKey
                it[SportsTable.minPlayersPerSide] = sport.minPlayersPerSide
                it[SportsTable.maxPlayersPerSide] = sport.maxPlayersPerSide
            }
        }

        // Load everything back (includes custom admin-added sports)
        val allSports = SportsTable.selectAll()
            .where { SportsTable.isActive eq true }
            .map {
                Sport(
                    code = it[SportsTable.code],
                    displayName = it[SportsTable.displayName],
                    iconKey = it[SportsTable.iconKey],
                    minPlayersPerSide = it[SportsTable.minPlayersPerSide],
                    maxPlayersPerSide = it[SportsTable.maxPlayersPerSide]
                )
            }
        KnownSports.registerAll(allSports)
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
