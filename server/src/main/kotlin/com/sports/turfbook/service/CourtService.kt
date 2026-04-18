package com.sports.turfbook.service

import com.sports.turfbook.api.dto.court.CourtDto
import com.sports.turfbook.api.dto.court.CourtPricingDto
import com.sports.turfbook.api.dto.court.CreateCourtDto
import com.sports.turfbook.api.dto.court.UpdateCourtDto
import com.sports.turfbook.database.tables.CourtSlotPricingTable
import com.sports.turfbook.database.tables.CourtsTable
import com.sports.turfbook.database.tables.TurfsTable
import com.sports.turfbook.database.tables.TurfSportsTable
import com.sports.turfbook.domain.KnownSports
import com.sports.turfbook.plugins.NotFoundException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class CourtService {

    fun createCourt(turfId: String, ownerId: String, dto: CreateCourtDto): CourtDto = transaction {
        val turfUuid = UUID.fromString(turfId)
        val ownerUuid = UUID.fromString(ownerId)

        // Verify ownership
        TurfsTable.selectAll()
            .where { (TurfsTable.id eq turfUuid) and (TurfsTable.ownerId eq ownerUuid) }
            .singleOrNull() ?: throw NotFoundException("Turf not found or not yours")

        val sportCode = dto.sport.uppercase()
        if (!KnownSports.isKnown(sportCode)) {
            throw IllegalArgumentException("Unknown sport: $sportCode")
        }

        val now: Instant = Clock.System.now()
        val courtId = UUID.randomUUID()

        CourtsTable.insert {
            it[id] = courtId
            it[CourtsTable.turfId] = turfUuid
            it[name] = dto.name
            it[sport] = sportCode
            it[description] = dto.description
            it[openingTime] = dto.openingTime
            it[closingTime] = dto.closingTime
            it[createdAt] = now
            it[updatedAt] = now
        }

        dto.pricing.forEach { pricing ->
            CourtSlotPricingTable.insert {
                it[CourtSlotPricingTable.courtId] = courtId
                it[durationMinutes] = pricing.durationMinutes
                it[priceInPaise] = pricing.priceInPaise
            }
        }

        // Keep TurfSportsTable in sync
        val alreadyListed = TurfSportsTable.selectAll()
            .where { (TurfSportsTable.turfId eq turfUuid) and (TurfSportsTable.sport eq sportCode) }
            .count() > 0
        if (!alreadyListed) {
            TurfSportsTable.insert {
                it[TurfSportsTable.turfId] = turfUuid
                it[TurfSportsTable.sport] = sportCode
            }
        }

        fetchCourtDto(courtId)!!
    }

    fun getCourt(courtId: String): CourtDto =
        transaction { fetchCourtDto(UUID.fromString(courtId)) }
            ?: throw NotFoundException("Court not found")

    fun getCourtsForTurf(turfId: String): List<CourtDto> = transaction {
        val turfUuid = UUID.fromString(turfId)
        CourtsTable.selectAll()
            .where { (CourtsTable.turfId eq turfUuid) and (CourtsTable.isActive eq true) }
            .orderBy(CourtsTable.sport to SortOrder.ASC, CourtsTable.createdAt to SortOrder.ASC)
            .mapNotNull { fetchCourtDto(it[CourtsTable.id].value) }
    }

    fun updateCourt(turfId: String, courtId: String, ownerId: String, dto: UpdateCourtDto): CourtDto = transaction {
        val turfUuid = UUID.fromString(turfId)
        val courtUuid = UUID.fromString(courtId)
        val ownerUuid = UUID.fromString(ownerId)

        TurfsTable.selectAll()
            .where { (TurfsTable.id eq turfUuid) and (TurfsTable.ownerId eq ownerUuid) }
            .singleOrNull() ?: throw NotFoundException("Turf not found or not yours")

        val now: Instant = Clock.System.now()
        val updated = CourtsTable.update({
            (CourtsTable.id eq courtUuid) and (CourtsTable.turfId eq turfUuid)
        }) { row ->
            dto.name?.let { row[name] = it }
            dto.description?.let { row[description] = it }
            dto.openingTime?.let { row[openingTime] = it }
            dto.closingTime?.let { row[closingTime] = it }
            dto.isActive?.let { row[isActive] = it }
            row[updatedAt] = now
        }

        if (updated == 0) throw NotFoundException("Court not found")

        dto.pricing?.let { newPricing ->
            CourtSlotPricingTable.deleteWhere {
                Op.build { CourtSlotPricingTable.courtId eq courtUuid }
            }
            newPricing.forEach { pricing ->
                CourtSlotPricingTable.insert {
                    it[CourtSlotPricingTable.courtId] = courtUuid
                    it[durationMinutes] = pricing.durationMinutes
                    it[priceInPaise] = pricing.priceInPaise
                }
            }
        }

        fetchCourtDto(courtUuid)!!
    }

    fun deactivateCourt(turfId: String, courtId: String, ownerId: String) = transaction {
        val turfUuid = UUID.fromString(turfId)
        val courtUuid = UUID.fromString(courtId)
        val ownerUuid = UUID.fromString(ownerId)

        TurfsTable.selectAll()
            .where { (TurfsTable.id eq turfUuid) and (TurfsTable.ownerId eq ownerUuid) }
            .singleOrNull() ?: throw NotFoundException("Turf not found or not yours")

        val updated = CourtsTable.update({
            (CourtsTable.id eq courtUuid) and (CourtsTable.turfId eq turfUuid)
        }) {
            it[isActive] = false
            it[updatedAt] = Clock.System.now()
        }

        if (updated == 0) throw NotFoundException("Court not found")
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    internal fun fetchCourtDto(courtId: UUID): CourtDto? {
        val row = CourtsTable.selectAll()
            .where { CourtsTable.id eq courtId }
            .singleOrNull() ?: return null

        val pricing = CourtSlotPricingTable.selectAll()
            .where { CourtSlotPricingTable.courtId eq courtId }
            .associate { it[CourtSlotPricingTable.durationMinutes] to it[CourtSlotPricingTable.priceInPaise] }

        return CourtDto(
            id = courtId.toString(),
            turfId = row[CourtsTable.turfId].toString(),
            name = row[CourtsTable.name],
            sport = row[CourtsTable.sport],
            description = row[CourtsTable.description],
            openingTime = row[CourtsTable.openingTime],
            closingTime = row[CourtsTable.closingTime],
            pricingByDuration = pricing,
            isActive = row[CourtsTable.isActive]
        )
    }
}
