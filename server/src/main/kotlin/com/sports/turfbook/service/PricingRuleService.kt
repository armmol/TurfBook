package com.sports.turfbook.service

import com.sports.turfbook.api.dto.pricing.CourtPricingRuleDto
import com.sports.turfbook.api.dto.pricing.CreateCourtPricingRuleDto
import com.sports.turfbook.api.dto.pricing.UpdateCourtPricingRuleDto
import com.sports.turfbook.database.tables.CourtPricingRulesTable
import com.sports.turfbook.database.tables.CourtsTable
import com.sports.turfbook.database.tables.TurfsTable
import com.sports.turfbook.plugins.NotFoundException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class PricingRuleService {

    fun createRule(turfId: String, courtId: String, ownerId: String, dto: CreateCourtPricingRuleDto): CourtPricingRuleDto =
        transaction {
            verifyOwnership(turfId, courtId, ownerId)

            if (dto.dayOfWeek != null && dto.dayOfWeek !in 1..7) {
                throw IllegalArgumentException("dayOfWeek must be 1–7 (Mon–Sun)")
            }

            val now: Instant = Clock.System.now()
            val ruleId = UUID.randomUUID()

            CourtPricingRulesTable.insert {
                it[id] = ruleId
                it[CourtPricingRulesTable.courtId] = UUID.fromString(courtId)
                it[durationMinutes] = dto.durationMinutes
                it[priceInPaise] = dto.priceInPaise
                it[dayOfWeek] = dto.dayOfWeek
                it[startTime] = dto.startTime
                it[endTime] = dto.endTime
                it[specificDate] = dto.specificDate
                it[priority] = dto.priority
                it[createdAt] = now
                it[updatedAt] = now
            }

            fetchRule(ruleId)!!
        }

    fun getRulesForCourt(turfId: String, courtId: String): List<CourtPricingRuleDto> = transaction {
        val courtUuid = UUID.fromString(courtId)
        // Validate court belongs to turf
        CourtsTable.selectAll()
            .where { (CourtsTable.id eq courtUuid) and (CourtsTable.turfId eq UUID.fromString(turfId)) }
            .singleOrNull() ?: throw NotFoundException("Court not found on this turf")

        CourtPricingRulesTable.selectAll()
            .where { (CourtPricingRulesTable.courtId eq courtUuid) and (CourtPricingRulesTable.isActive eq true) }
            .orderBy(
                CourtPricingRulesTable.priority to SortOrder.DESC,
                CourtPricingRulesTable.createdAt to SortOrder.ASC
            )
            .map { it.toDto() }
    }

    fun updateRule(
        turfId: String, courtId: String, ruleId: String,
        ownerId: String, dto: UpdateCourtPricingRuleDto
    ): CourtPricingRuleDto = transaction {
        verifyOwnership(turfId, courtId, ownerId)

        val ruleUuid = UUID.fromString(ruleId)
        val now: Instant = Clock.System.now()

        val updated = CourtPricingRulesTable.update({
            (CourtPricingRulesTable.id eq ruleUuid) and
            (CourtPricingRulesTable.courtId eq UUID.fromString(courtId))
        }) { row ->
            dto.priceInPaise?.let { row[priceInPaise] = it }
            dto.priority?.let { row[priority] = it }
            dto.isActive?.let { row[isActive] = it }

            when {
                dto.clearDayOfWeek -> row[dayOfWeek] = null
                dto.dayOfWeek != null -> row[dayOfWeek] = dto.dayOfWeek
            }
            when {
                dto.clearStartTime -> row[startTime] = null
                dto.startTime != null -> row[startTime] = dto.startTime
            }
            when {
                dto.clearEndTime -> row[endTime] = null
                dto.endTime != null -> row[endTime] = dto.endTime
            }
            when {
                dto.clearSpecificDate -> row[specificDate] = null
                dto.specificDate != null -> row[specificDate] = dto.specificDate
            }

            row[updatedAt] = now
        }

        if (updated == 0) throw NotFoundException("Pricing rule not found")
        fetchRule(ruleUuid)!!
    }

    fun deleteRule(turfId: String, courtId: String, ruleId: String, ownerId: String) = transaction {
        verifyOwnership(turfId, courtId, ownerId)

        val deleted = CourtPricingRulesTable.deleteWhere {
            Op.build {
                (CourtPricingRulesTable.id eq UUID.fromString(ruleId)) and
                (CourtPricingRulesTable.courtId eq UUID.fromString(courtId))
            }
        }
        if (deleted == 0) throw NotFoundException("Pricing rule not found")
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun verifyOwnership(turfId: String, courtId: String, ownerId: String) {
        val turfUuid = UUID.fromString(turfId)
        val courtUuid = UUID.fromString(courtId)
        val ownerUuid = UUID.fromString(ownerId)

        TurfsTable.selectAll()
            .where { (TurfsTable.id eq turfUuid) and (TurfsTable.ownerId eq ownerUuid) }
            .singleOrNull() ?: throw NotFoundException("Turf not found or not yours")

        CourtsTable.selectAll()
            .where { (CourtsTable.id eq courtUuid) and (CourtsTable.turfId eq turfUuid) }
            .singleOrNull() ?: throw NotFoundException("Court not found on this turf")
    }

    private fun fetchRule(ruleId: UUID): CourtPricingRuleDto? =
        CourtPricingRulesTable.selectAll()
            .where { CourtPricingRulesTable.id eq ruleId }
            .singleOrNull()?.toDto()

    private fun ResultRow.toDto() = CourtPricingRuleDto(
        id = this[CourtPricingRulesTable.id].toString(),
        courtId = this[CourtPricingRulesTable.courtId].toString(),
        durationMinutes = this[CourtPricingRulesTable.durationMinutes],
        priceInPaise = this[CourtPricingRulesTable.priceInPaise],
        dayOfWeek = this[CourtPricingRulesTable.dayOfWeek],
        startTime = this[CourtPricingRulesTable.startTime],
        endTime = this[CourtPricingRulesTable.endTime],
        specificDate = this[CourtPricingRulesTable.specificDate],
        priority = this[CourtPricingRulesTable.priority],
        isActive = this[CourtPricingRulesTable.isActive]
    )
}
