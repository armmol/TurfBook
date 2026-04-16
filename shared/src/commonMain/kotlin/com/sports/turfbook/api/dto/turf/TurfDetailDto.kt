package com.sports.turfbook.api.dto.turf

import kotlinx.serialization.Serializable

/** Full turf details for the Turf Detail screen */
@Serializable
data class TurfDetailDto(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    /** Sport codes, e.g. ["FOOTBALL", "CRICKET"]. Use KnownSports.fromCode() on client. */
    val sports: List<String>,
    val amenities: List<String>,
    val photoUrls: List<String>,
    /** "06:00" 24h format */
    val openingTime: String,
    /** "23:00" 24h format */
    val closingTime: String,
    val slotDurationsMinutes: List<Int>,
    val rating: Float,
    val reviewCount: Int,
    val isVerified: Boolean,
    val distanceKm: Float? = null,
    /** Non-null when the turf is connected to an external booking system */
    val externalSystemType: String? = null
)
