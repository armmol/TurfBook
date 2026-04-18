package com.sports.turfbook.api.dto.booking

import kotlinx.serialization.Serializable

/** POST /bookings */
@Serializable
data class CreateBookingDto(
    val turfId: String,
    /** Court UUID — sport and pricing are derived from the court. */
    val courtId: String,
    /** "2024-01-15" */
    val date: String,
    /** "10:00" 24h */
    val startTime: String,
    /** 30 or 60 */
    val durationMinutes: Int
)
