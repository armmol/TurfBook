package com.sports.turfbook.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String? = null,
    val turfId: String,
    /** 1–5 stars */
    val rating: Int,
    val comment: String? = null,
    /** Booking ID this review is tied to (one review per booking) */
    val bookingId: String,
    val createdAt: String
)
