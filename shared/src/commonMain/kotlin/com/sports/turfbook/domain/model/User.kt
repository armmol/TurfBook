package com.sports.turfbook.domain.model

import com.sports.turfbook.domain.enums.SportType
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
    val preferredSports: List<SportType> = emptyList(),
    /** Total bookings made (shown on profile) */
    val totalBookings: Int = 0,
    /** ISO 8601, e.g. "2024-01-15T10:30:00Z" */
    val createdAt: String,
    val updatedAt: String
)
