package com.sports.turfbook.api.dto.turf

import kotlinx.serialization.Serializable

/** Lightweight turf summary used in search results and map view */
@Serializable
data class TurfListItemDto(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    /** Sport codes, e.g. ["FOOTBALL", "CRICKET"]. Use KnownSports.fromCode() on client. */
    val sports: List<String>,
    val coverPhotoUrl: String? = null,
    val rating: Float,
    val reviewCount: Int,
    /** Lowest slot price across all sports/durations, in paise */
    val minPriceInPaise: Long,
    val isVerified: Boolean,
    /** Populated when user location is available */
    val distanceKm: Float? = null
)
