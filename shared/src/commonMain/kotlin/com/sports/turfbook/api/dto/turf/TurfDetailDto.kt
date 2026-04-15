package com.sports.turfbook.api.dto.turf

import com.sports.turfbook.domain.enums.SportType
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
    val sports: List<SportType>,
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
    val distanceKm: Float? = null
)
