package com.sports.turfbook.service

import com.sports.turfbook.database.tables.OtpCodesTable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

class OtpService(private val smsProvider: SmsProvider) {

    private val logger = LoggerFactory.getLogger(OtpService::class.java)

    /** Generates and sends a 6-digit OTP. Returns the code in DEV_MODE only. */
    suspend fun sendOtp(phone: String): String? {
        val otp = (100_000..999_999).random().toString()
        val now: Instant = Clock.System.now()
        val expiresAt: Instant = now + 5.minutes

        transaction {
            OtpCodesTable.insert {
                it[id] = UUID.randomUUID()
                it[OtpCodesTable.phone] = phone
                it[code] = otp
                it[OtpCodesTable.expiresAt] = expiresAt
                it[createdAt] = now
            }
        }

        smsProvider.sendOtp(phone, otp)

        return if (System.getenv("DEV_MODE") == "true") otp else null
    }

    /** Returns true and marks the OTP used if valid; false otherwise. */
    fun verifyOtp(phone: String, code: String): Boolean {
        val now: Instant = Clock.System.now()

        return transaction {
            val row = OtpCodesTable
                .selectAll()
                .where {
                    (OtpCodesTable.phone eq phone) and
                    (OtpCodesTable.code eq code) and
                    (OtpCodesTable.isUsed eq false) and
                    (OtpCodesTable.expiresAt greater now)
                }
                .orderBy(OtpCodesTable.createdAt)
                .lastOrNull() ?: return@transaction false

            OtpCodesTable.update({ OtpCodesTable.id eq row[OtpCodesTable.id] }) {
                it[isUsed] = true
            }
            true
        }
    }
}

interface SmsProvider {
    suspend fun sendOtp(phone: String, otp: String)
}

/** Used in local development — logs OTP to console instead of sending SMS */
class ConsoleSmsProvider : SmsProvider {
    private val logger = LoggerFactory.getLogger(ConsoleSmsProvider::class.java)
    override suspend fun sendOtp(phone: String, otp: String) {
        logger.info("📱 [DEV] OTP for $phone → $otp")
    }
}

/**
 * Production SMS provider stub.
 * Replace with MSG91 / Twilio SDK calls.
 */
class Msg91SmsProvider(private val apiKey: String, private val templateId: String) : SmsProvider {
    private val logger = LoggerFactory.getLogger(Msg91SmsProvider::class.java)
    override suspend fun sendOtp(phone: String, otp: String) {
        // TODO: implement MSG91 HTTP call
        logger.warn("MSG91 not yet integrated — OTP for $phone: $otp")
    }
}
