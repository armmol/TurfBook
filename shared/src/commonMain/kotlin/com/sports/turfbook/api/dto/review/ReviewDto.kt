package com.sports.turfbook.api.dto.review

import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: String,
    val userId: String,
    val userName: String,
    val userPhotoUrl: String? = null,
    val turfId: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String
)

/** POST /turfs/{turfId}/reviews */
@Serializable
data class CreateReviewDto(
    /** Must match a COMPLETED booking by this user for this turf */
    val bookingId: String,
    /** 1–5 */
    val rating: Int,
    val comment: String? = null
)
