package com.sports.turfbook.api.dto.slot

import kotlinx.serialization.Serializable

/** All slots for a given turf on a given date — used to render the booking calendar */
@Serializable
data class DayAvailabilityDto(
    val turfId: String,
    /** "2024-01-15" */
    val date: String,
    val slots: List<SlotDto>
)
