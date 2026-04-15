package com.sports.turfbook.api.dto.slot

import com.sports.turfbook.domain.enums.SlotStatus
import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

@Serializable
data class SlotDto(
    val id: String,
    val sport: SportType,
    /** "10:00" */
    val startTime: String,
    /** "11:00" */
    val endTime: String,
    val durationMinutes: Int,
    /** Price in paise */
    val priceInPaise: Long,
    val status: SlotStatus
)
