package com.sports.turfbook.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class BookingStatus {
    /** Created, waiting for payment */
    PENDING_PAYMENT,
    /** Payment received, booking confirmed */
    CONFIRMED,
    /** Cancelled by user or turf owner */
    CANCELLED,
    /** Slot time has passed, booking fulfilled */
    COMPLETED,
    /** Payment refunded */
    REFUNDED
}
