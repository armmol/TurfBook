package com.sports.turfbook.service

import com.sports.turfbook.database.tables.SportsTable
import com.sports.turfbook.domain.KnownSports
import com.sports.turfbook.domain.Sport
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

/**
 * Manages the persisted sport registry.
 *
 * The in-memory [KnownSports] registry is the source of truth at runtime;
 * [SportsTable] makes registrations survive server restarts.
 */
class SportsRegistryService {

    /**
     * Registers a sport in the DB and the in-memory registry.
     * Safe to call multiple times — uses upsert.
     */
    fun registerSport(sport: Sport) {
        transaction {
            SportsTable.upsert(SportsTable.code) {
                it[SportsTable.code] = sport.code
                it[SportsTable.displayName] = sport.displayName
                it[SportsTable.iconKey] = sport.iconKey
                it[SportsTable.minPlayersPerSide] = sport.minPlayersPerSide
                it[SportsTable.maxPlayersPerSide] = sport.maxPlayersPerSide
                it[SportsTable.isActive] = true
            }
        }
        KnownSports.register(sport)
    }
}
