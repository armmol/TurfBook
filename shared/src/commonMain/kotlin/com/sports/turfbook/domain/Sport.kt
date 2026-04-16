package com.sports.turfbook.domain

import kotlinx.serialization.Serializable

/**
 * Represents a sport that can be played at a turf.
 * Sports are identified by their [code] (e.g. "FOOTBALL").
 * Use [KnownSports] for predefined sport instances; register custom sports
 * with [KnownSports.register] to extend the system without code changes.
 */
@Serializable
data class Sport(
    /** Unique identifier used in API and DB, e.g. "FOOTBALL". Always uppercase. */
    val code: String,
    /** Human-readable name, e.g. "Football". */
    val displayName: String,
    /** Key used by the mobile app to look up sport-specific icons. */
    val iconKey: String = "",
    /** Typical minimum team/player count per side. */
    val minPlayersPerSide: Int = 2,
    /** Typical maximum team/player count per side. */
    val maxPlayersPerSide: Int = 22
)
