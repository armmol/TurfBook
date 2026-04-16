package com.sports.turfbook.connector

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

// ── Payload types sent to external systems ───────────────────────────────────

@Serializable
data class ExternalBookingPayload(
    val bookingId: String,
    val turfId: String,
    val userId: String,
    val sport: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val amountInPaise: Long,
    val status: String
)

@Serializable
data class ExternalConfirmPayload(
    val bookingId: String,
    val gatewayPaymentId: String,
    val status: String = "CONFIRMED"
)

@Serializable
data class ExternalCancelPayload(
    val bookingId: String,
    val reason: String?,
    val status: String = "CANCELLED"
)

// ── Interface ────────────────────────────────────────────────────────────────

/**
 * Abstraction over an external (turf-owned) local booking system.
 *
 * Implementations must be fault-tolerant — a failure to reach an external
 * system must never roll back a TurfBook transaction. Log and continue.
 */
interface ExternalBookingConnector {

    /**
     * Called when a booking is created (status = PENDING_PAYMENT).
     * The external system can use this to hold the slot in its own UI.
     */
    fun onBookingCreated(payload: ExternalBookingPayload)

    /**
     * Called after payment is verified and booking moves to CONFIRMED.
     */
    fun onBookingConfirmed(payload: ExternalConfirmPayload)

    /**
     * Called when a booking is cancelled.
     */
    fun onBookingCancelled(payload: ExternalCancelPayload)

    /**
     * Optional: check slot availability in the external system.
     * Returns true if available, or true if the connector does not support this.
     */
    fun isSlotAvailable(turfId: String, date: String, startTime: String, sport: String): Boolean = true
}

// ── No-op implementation (default for turfs without external systems) ────────

object NoOpConnector : ExternalBookingConnector {
    override fun onBookingCreated(payload: ExternalBookingPayload) = Unit
    override fun onBookingConfirmed(payload: ExternalConfirmPayload) = Unit
    override fun onBookingCancelled(payload: ExternalCancelPayload) = Unit
}

// ── HTTP REST implementation ─────────────────────────────────────────────────

/**
 * Calls a turf's own REST API to sync bookings using Java's built-in HTTP client.
 *
 * Expected endpoints on the external system:
 *   POST {baseUrl}/bookings          → booking created
 *   POST {baseUrl}/bookings/confirm  → booking confirmed
 *   POST {baseUrl}/bookings/cancel   → booking cancelled
 *   GET  {baseUrl}/availability?turfId=&date=&startTime=&sport=
 *
 * All requests use JSON. Authorization: Bearer {apiKey} if provided.
 */
class HttpExternalBookingConnector(
    private val baseUrl: String,
    private val apiKey: String?
) : ExternalBookingConnector {

    private val log = LoggerFactory.getLogger(javaClass)
    private val json = Json { ignoreUnknownKeys = true }
    private val http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    override fun onBookingCreated(payload: ExternalBookingPayload) {
        post("$baseUrl/bookings", json.encodeToString(payload))
    }

    override fun onBookingConfirmed(payload: ExternalConfirmPayload) {
        post("$baseUrl/bookings/confirm", json.encodeToString(payload))
    }

    override fun onBookingCancelled(payload: ExternalCancelPayload) {
        post("$baseUrl/bookings/cancel", json.encodeToString(payload))
    }

    override fun isSlotAvailable(turfId: String, date: String, startTime: String, sport: String): Boolean {
        return try {
            val url = "$baseUrl/availability?turfId=$turfId&date=$date&startTime=$startTime&sport=$sport"
            val request = buildRequest(url).GET().build()
            val response = http.send(request, HttpResponse.BodyHandlers.ofString())
            response.statusCode() in 200..299
        } catch (e: Exception) {
            log.warn("External availability check failed [$baseUrl]: ${e.message} — defaulting to available")
            true // Fail open: don't block bookings if external system is unreachable
        }
    }

    private fun post(url: String, body: String) {
        try {
            val request = buildRequest(url)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .build()
            val response = http.send(request, HttpResponse.BodyHandlers.discarding())
            if (response.statusCode() !in 200..299) {
                log.warn("External system returned ${response.statusCode()} for POST $url")
            }
        } catch (e: Exception) {
            log.warn("External booking sync failed [POST $url]: ${e.message}")
            // Intentionally swallowed — external system outages must not affect TurfBook
        }
    }

    private fun buildRequest(url: String): HttpRequest.Builder {
        val builder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(8))
        apiKey?.let { builder.header("Authorization", "Bearer $it") }
        return builder
    }
}

// ── Factory ──────────────────────────────────────────────────────────────────

object ConnectorFactory {
    fun create(systemType: String?, baseUrl: String?, apiKey: String?): ExternalBookingConnector {
        if (systemType == null || baseUrl == null) return NoOpConnector
        return when (systemType.uppercase()) {
            "REST_API", "WEBHOOK" -> HttpExternalBookingConnector(baseUrl, apiKey)
            else -> NoOpConnector
        }
    }
}
