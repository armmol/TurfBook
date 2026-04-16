package com.sports.turfbook.service

import java.util.Base64

object QrService {
    /**
     * Generates a base64-encoded string the mobile app renders as a QR code.
     * Format: TURFBOOK|{bookingId}|{turfId}|{date}|{startTime}
     */
    fun generate(bookingId: String, turfId: String, date: String, startTime: String): String {
        val content = "TURFBOOK|$bookingId|$turfId|$date|$startTime"
        return Base64.getEncoder().encodeToString(content.toByteArray())
    }
}
