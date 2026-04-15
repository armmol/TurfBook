package com.sports.turfbook.database.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object OtpCodesTable : UUIDTable("otp_codes") {
    val phone = varchar("phone", 15).index()
    val code = varchar("code", 6)
    val expiresAt = timestamp("expires_at")
    val isUsed = bool("is_used").default(false)
    val createdAt = timestamp("created_at")
}
