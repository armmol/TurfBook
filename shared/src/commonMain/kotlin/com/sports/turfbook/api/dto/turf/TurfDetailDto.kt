package com.sports.turfbook.api.dto.turf

import com.sports.turfbook.api.dto.court.CourtDto
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
    /** All active courts, grouped by sport on the client side */
    val courts: List<CourtDto>,
    /** Derived from courts — distinct sport codes offered at this turf */
    val sports: List<String>,
    val amenities: List<String>,
    val photoUrls: List<String>,
    /** "06:00" 24h — default for courts that don't override */
    val openingTime: String,
    val closingTime: String,
    val rating: Float,
    val reviewCount: Int,
    val isVerified: Boolean,
    val distanceKm: Float? = null,
    /** Non-null when turf is integrated with an external booking system */
    val externalSystemType: String? = null
)
