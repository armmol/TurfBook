package com.sports.turfbook.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentStatus {
    INITIATED,
    SUCCESS,
    FAILED,
    REFUND_INITIATED,
    REFUNDED
}
