package com.sports.turfbook.domain.enums

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentGateway(val displayName: String) {
    RAZORPAY("Razorpay"),
    PHONEPE("PhonePe"),
    PAYTM("Paytm")
}
