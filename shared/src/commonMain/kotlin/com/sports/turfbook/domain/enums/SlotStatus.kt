package com.sports.turfbook.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class SlotStatus {
    /** Available to book */
    AVAILABLE,
    /** Temporarily held (Redis lock, max 5 minutes) while payment is in progress */
    LOCKED,
    /** Confirmed booking exists */
    BOOKED,
    /** Blocked by turf owner (maintenance, event, etc.) */
    BLOCKED
}
