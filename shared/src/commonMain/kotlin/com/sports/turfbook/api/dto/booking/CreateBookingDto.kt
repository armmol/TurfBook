package com.sports.turfbook.api.dto.booking

import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

/** POST /bookings */
@Serializable
data class CreateBookingDto(
    val slotId: String,
    val sport: SportType
)
