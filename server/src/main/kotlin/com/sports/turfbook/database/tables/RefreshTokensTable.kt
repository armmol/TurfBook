package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object RefreshTokensTable : UUIDTable("refresh_tokens") {
    val userId = reference("user_id", UsersTable)
    val token = varchar("token", 36).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val createdAt = timestamp("created_at")
}
