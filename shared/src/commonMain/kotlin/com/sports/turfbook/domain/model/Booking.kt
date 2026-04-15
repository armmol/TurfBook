package com.sports.turfbook.domain.model

import com.sports.turfbook.domain.enums.BookingStatus
import com.sports.turfbook.domain.enums.SportType
import kotlinx.serialization.Serializable

@Serializable
data class Booking(
    val id: String,
    val userId: String,
    val turfId: String,
    val turfName: String,
    val turfAddress: String,
    val slotId: String,
    val sport: SportType,
    /** "2024-01-15" */
    val date: String,
    /** "10:00" */
    val startTime: String,
    /** "11:00" */
    val endTime: String,
    /** Price in paise */
    val amountInPaise: Long,
    val status: BookingStatus,
    val paymentId: String? = null,
    /** Base64-encoded QR content for check-in at the turf */
    val qrCode: String? = null,
    val cancellationReason: String? = null,
    val createdAt: String,
    val updatedAt: String
)
