package com.sports.turfbook.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    /** Verified Indian mobile number, e.g. "+919876543210" */
    val phone: String,
    val name: String,
    val email: String? = null,
    val profilePhotoUrl: String? = null,
    /** City the user primarily plays in, e.g. "Mumbai" */
    val city: String,
    /** Sport codes, e.g. ["FOOTBALL", "CRICKET"]. Use KnownSports.fromCode() to resolve. */
    val preferredSports: List<String> = emptyList(),
    /** Total bookings made (shown on profile) */
    val totalBookings: Int = 0,
    /** ISO 8601, e.g. "2024-01-15T10:30:00Z" */
    val createdAt: String,
    val updatedAt: String
)
