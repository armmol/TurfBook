package com.sports.turfbook.domain.model

import com.sports.turfbook.domain.enums.SlotStatus
import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

@Serializable
data class TurfSlot(
    val id: String,
    val turfId: String,
    val sport: SportType,
    /** "2024-01-15" */
    val date: String,
    /** "10:00" 24h format */
    val startTime: String,
    /** "11:00" 24h format */
    val endTime: String,
    /** 30 or 60 */
    val durationMinutes: Int,
    /** Price in paise (1 INR = 100 paise) to avoid floating point issues */
    val priceInPaise: Long,
    val status: SlotStatus,
    /** Optional player limit for the sport/slot */
    val maxPlayers: Int? = null
)
