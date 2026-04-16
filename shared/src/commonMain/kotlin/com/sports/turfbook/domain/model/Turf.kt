package com.sports.turfbook.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Turf(
    val id: String,
    val ownerId: String,
    val name: String,
    val description: String,
    val address: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    /** Sport codes, e.g. ["FOOTBALL", "CRICKET"]. Use KnownSports.fromCode() to resolve. */
    val sports: List<String>,
    val amenities: List<String>,
    val photoUrls: List<String>,
    /** "06:00" 24h format */
    val openingTime: String,
    /** "23:00" 24h format */
    val closingTime: String,
    /** Available slot durations in minutes: [30], [60], or [30, 60] */
    val slotDurationsMinutes: List<Int>,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    /** Non-null when turf is integrated with an external booking system */
    val externalSystemType: String? = null,
    val createdAt: String,
    val updatedAt: String
)
