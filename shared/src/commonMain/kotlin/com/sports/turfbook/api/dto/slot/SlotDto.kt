package com.sports.turfbook.api.dto.slot

import com.sports.turfbook.domain.enums.SlotStatus
import kotlinx.serialization.Serializable

@Serializable
data class SlotDto(
    val id: String,
    /** "10:00" */
    val startTime: String,
    /** "11:00" */
    val endTime: String,
    val durationMinutes: Int,
    /** Price in paise */
    val priceInPaise: Long,
    val status: SlotStatus
)
