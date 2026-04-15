package com.sports.turfbook.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import java.util.UUID

object JwtUtil {
    private val secret: String = System.getenv("JWT_SECRET") ?: "dev-secret-change-before-production"
    private val issuer: String = "turfbook"
    private val audience: String = "turfbook-users"
    val realm: String = "TurfBook"

    /** 15-minute signed access token */
    fun generateAccessToken(userId: String, phone: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId)
            .withClaim("phone", phone)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 15 * 60 * 1_000L))
            .sign(Algorithm.HMAC256(secret))

    /** Opaque random token stored in DB; revokable */
    fun generateRefreshToken(): String = UUID.randomUUID().toString()

    fun verifier() = JWT
        .require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun userId(token: String): String =
        JWT.decode(token).subject

    fun userPhone(token: String): String =
        JWT.decode(token).getClaim("phone").asString()
}
