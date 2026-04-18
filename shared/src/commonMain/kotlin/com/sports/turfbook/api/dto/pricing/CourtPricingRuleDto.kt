package com.sports.turfbook.api.dto.pricing

import kotlinx.serialization.Serializable

/**
 * A pricing rule for a court. Rules are evaluated in descending [priority] order;
 * the first matching rule wins. Unset conditions match anything.
 *
 * Condition fields:
 * - [specificDate]  — exact date "YYYY-MM-DD"; highest specificity
 * - [dayOfWeek]     — 1=Mon … 7=Sun (ISO); repeating weekly rule
 * - [startTime] / [endTime] — "HH:MM" 24h window; slot's startTime must be
 *                             ≥ startTime and < endTime
 *
 * Resolution: base price in CourtSlotPricingTable is used when no rule matches.
 */
@Serializable
data class CourtPricingRuleDto(
    val id: String,
    val courtId: String,
    val durationMinutes: Int,
    val priceInPaise: Long,
    /** 1=Mon … 7=Sun (ISO). Null = any day of week. */
    val dayOfWeek: Int? = null,
    /** Slot startTime must be ≥ this. "18:00". Null = no lower bound. */
    val startTime: String? = null,
    /** Slot startTime must be < this. "22:00". Null = no upper bound. */
    val endTime: String? = null,
    /** "2024-12-25" — one-off date override. Takes precedence over dayOfWeek. */
    val specificDate: String? = null,
    /** Higher value wins when multiple rules match the same slot. */
    val priority: Int = 0,
    val isActive: Boolean = true
)

/** POST /turfs/{id}/courts/{courtId}/pricing-rules */
@Serializable
data class CreateCourtPricingRuleDto(
    val durationMinutes: Int,
    val priceInPaise: Long,
    val dayOfWeek: Int? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val specificDate: String? = null,
    val priority: Int = 0
)

/** PATCH /turfs/{id}/courts/{courtId}/pricing-rules/{ruleId} */
@Serializable
data class UpdateCourtPricingRuleDto(
    val priceInPaise: Long? = null,
    val dayOfWeek: Int? = null,
    val clearDayOfWeek: Boolean = false,
    val startTime: String? = null,
    val clearStartTime: Boolean = false,
    val endTime: String? = null,
    val clearEndTime: Boolean = false,
    val specificDate: String? = null,
    val clearSpecificDate: Boolean = false,
    val priority: Int? = null,
    val isActive: Boolean? = null
)
