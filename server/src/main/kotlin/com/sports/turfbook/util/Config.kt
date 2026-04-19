package com.sports.turfbook.util

object Config {
    val googleMapsApiKey: String
        get() = System.getenv("GOOGLE_MAPS_API_KEY")
            ?: error("GOOGLE_MAPS_API_KEY environment variable not set")

    val msg91ApiKey: String?
        get() = System.getenv("MSG91_API_KEY")?.takeIf { it.isNotBlank() }

    val msg91SenderId: String?
        get() = System.getenv("MSG91_SENDER_ID")?.takeIf { it.isNotBlank() }

    val msg91TemplateId: String?
        get() = System.getenv("MSG91_TEMPLATE_ID")?.takeIf { it.isNotBlank() }

    val razorpayKeyId: String?
        get() = System.getenv("RAZORPAY_KEY_ID")?.takeIf { it.isNotBlank() }

    val razorpayKeySecret: String?
        get() = System.getenv("RAZORPAY_KEY_SECRET")?.takeIf { it.isNotBlank() }
}
