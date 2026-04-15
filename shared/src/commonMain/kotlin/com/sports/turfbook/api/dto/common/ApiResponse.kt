package com.sports.turfbook.api.dto.common

import kotlinx.serialization.Serializable

/**
 * Standard envelope for every API response.
 *
 * Success:  ApiResponse(success=true,  data=..., error=null)
 * Failure:  ApiResponse(success=false, data=null, error="message")
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)
