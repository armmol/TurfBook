package com.sports.turfbook.api.dto.booking

import com.sports.turfbook.domain.enums.BookingStatus
import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

@Serializable
data class BookingDto(
    val id: String,
    val turfId: String,
    val turfName: String,
    val turfAddress: String,
    val sport: SportType,
    /** "2024-01-15" */
    val date: String,
    /** "10:00" */
    val startTime: String,
    /** "11:00" */
    val endTime: String,
    /** Amount in paise */
    val amountInPaise: Long,
    val status: BookingStatus,
    /** Base64 QR content, available once booking is CONFIRMED */
    val qrCode: String? = null,
    val cancellationReason: String? = null,
    val createdAt: String
)
