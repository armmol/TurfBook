package com.sports.turfbook.service

import com.sports.turfbook.api.dto.auth.AuthResponseDto
import com.sports.turfbook.database.tables.RefreshTokensTable
import com.sports.turfbook.util.JwtUtil
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import kotlin.time.Duration.Companion.days

class AuthService(private val userService: UserService) {

    /**
     * Called after OTP is verified.
     * Creates the user if they don't exist, then issues JWT + refresh token.
     */
    fun loginOrRegister(phone: String): AuthResponseDto {
        val existingUser = userService.findByPhone(phone)
        val isNewUser = existingUser == null
        val user = existingUser ?: userService.createUser(phone)

        val accessToken = JwtUtil.generateAccessToken(user.id, user.phone)
        val refreshToken = storeRefreshToken(user.id)

        return AuthResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = user,
            isNewUser = isNewUser
        )
    }

    /**
     * Exchanges a valid refresh token for a new token pair (rotation).
     * Returns null if the token is expired, revoked, or not found.
     */
    fun refreshTokens(token: String): Pair<String, String>? {
        val now: Instant = Clock.System.now()

        return transaction {
            val row = RefreshTokensTable
                .selectAll()
                .where {
                    (RefreshTokensTable.token eq token) and
                    (RefreshTokensTable.isRevoked eq false) and
                    (RefreshTokensTable.expiresAt greater now)
                }
                .singleOrNull() ?: return@transaction null

            val userId = row[RefreshTokensTable.userId].toString()

            // Revoke the used token (rotation — prevents replay)
            RefreshTokensTable.update({ RefreshTokensTable.token eq token }) {
                it[isRevoked] = true
            }

            val user = userService.findById(userId) ?: return@transaction null
            val newAccess = JwtUtil.generateAccessToken(userId, user.phone)
            val newRefresh = storeRefreshToken(userId)

            newAccess to newRefresh
        }
    }

    /** Revokes a refresh token on logout */
    fun revokeRefreshToken(token: String) = transaction {
        RefreshTokensTable.update({ RefreshTokensTable.token eq token }) {
            it[isRevoked] = true
        }
    }

    private fun storeRefreshToken(userId: String): String {
        val token = JwtUtil.generateRefreshToken()
        val now: Instant = Clock.System.now()

        transaction {
            RefreshTokensTable.insert {
                it[id] = UUID.randomUUID()
                it[RefreshTokensTable.userId] = UUID.fromString(userId)
                it[RefreshTokensTable.token] = token
                it[expiresAt] = now + 30.days
                it[createdAt] = now
            }
        }
        return token
    }
}
