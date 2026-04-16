package com.sports.turfbook.database.tables

import org.jetbrains.exposed.sql.Table

/**
 * Persists all sports known to the system — both well-known ones seeded from
 * [com.sports.turfbook.domain.KnownSports] and any admin-registered custom sports.
 *
 * The server loads this table at startup and calls [com.sports.turfbook.domain.KnownSports.registerAll]
 * so that custom sports are available system-wide without restarts.
 */
object SportsTable : Table("sports") {
    /** Unique uppercase code, e.g. "FOOTBALL". Primary key. */
    val code = varchar("code", 50)
    val displayName = varchar("display_name", 100)
    /** Mobile icon lookup key, e.g. "ic_football" */
    val iconKey = varchar("icon_key", 100).default("")
    val minPlayersPerSide = integer("min_players_per_side").default(2)
    val maxPlayersPerSide = integer("max_players_per_side").default(22)
    /** Soft-delete: inactive sports are hidden from turf creation UI */
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(code)
}
