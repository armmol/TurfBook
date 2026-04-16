package com.sports.turfbook.service

import com.sports.turfbook.api.dto.turf.*
import com.sports.turfbook.connector.ConnectorFactory
import com.sports.turfbook.database.tables.*
import com.sports.turfbook.domain.KnownSports
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.math.*

class TurfService {

    fun createTurf(ownerId: String, dto: CreateTurfDto): TurfDetailDto = transaction {
        val now: Instant = Clock.System.now()
        val turfId = UUID.randomUUID()

        // Validate sports
        dto.slotPricing.forEach { pricing ->
            val code = pricing.sport.uppercase()
            if (!KnownSports.isKnown(code)) {
                throw IllegalArgumentException("Unknown sport: $code")
            }
        }

        TurfsTable.insert {
            it[id] = turfId
            it[TurfsTable.ownerId] = UUID.fromString(ownerId)
            it[name] = dto.name
            it[description] = dto.description
            it[address] = dto.address
            it[city] = dto.city
            it[latitude] = dto.latitude
            it[longitude] = dto.longitude
            it[openingTime] = dto.openingTime
            it[closingTime] = dto.closingTime
            it[createdAt] = now
            it[updatedAt] = now
        }

        dto.slotPricing.map { it.sport.uppercase() }.distinct().forEach { sport ->
            TurfSportsTable.insert {
                it[TurfSportsTable.turfId] = turfId
                it[TurfSportsTable.sport] = sport
            }
        }

        dto.amenities.forEach { amenity ->
            TurfAmenitiesTable.insert {
                it[TurfAmenitiesTable.turfId] = turfId
                it[TurfAmenitiesTable.amenity] = amenity
            }
        }

        dto.photoUrls.forEachIndexed { index, url ->
            TurfPhotosTable.insert {
                it[TurfPhotosTable.turfId] = turfId
                it[TurfPhotosTable.url] = url
                it[TurfPhotosTable.displayOrder] = index
            }
        }

        dto.slotPricing.forEach { pricing ->
            TurfSlotPricingTable.insert {
                it[TurfSlotPricingTable.turfId] = turfId
                it[TurfSlotPricingTable.sport] = pricing.sport.uppercase()
                it[TurfSlotPricingTable.durationMinutes] = pricing.durationMinutes
                it[TurfSlotPricingTable.priceInPaise] = pricing.priceInPaise
            }
        }

        // Register optional external booking system
        val extUrl = dto.externalSystemUrl
        val extType = dto.externalSystemType
        if (extUrl != null && extType != null) {
            TurfExternalSystemTable.insert {
                it[TurfExternalSystemTable.turfId] = turfId
                it[TurfExternalSystemTable.systemType] = extType.uppercase()
                it[TurfExternalSystemTable.baseUrl] = extUrl
                it[TurfExternalSystemTable.apiKey] = dto.externalSystemApiKey
            }
        }

        getTurfDetail(turfId.toString())!!
    }

    fun getTurfById(id: String): TurfDetailDto? = transaction {
        getTurfDetail(id)
    }

    fun searchTurfs(dto: TurfSearchQueryDto): List<TurfListItemDto> = transaction {
        val sportFilter = dto.sport?.uppercase()

        val query = TurfsTable.selectAll().where {
            var condition: Op<Boolean> = TurfsTable.isActive eq true
            dto.city?.let { city -> condition = condition and (TurfsTable.city.lowerCase() like "%${city.lowercase()}%") }
            condition
        }

        val turfs = query.map { row ->
            val turfId = row[TurfsTable.id].value

            val sports = TurfSportsTable
                .selectAll().where { TurfSportsTable.turfId eq turfId }
                .map { it[TurfSportsTable.sport] }

            if (sportFilter != null && sportFilter !in sports) return@map null

            val minPrice = TurfSlotPricingTable
                .selectAll().where { TurfSlotPricingTable.turfId eq turfId }
                .minOfOrNull { it[TurfSlotPricingTable.priceInPaise] } ?: 0L

            val coverPhoto = TurfPhotosTable
                .selectAll()
                .where { TurfPhotosTable.turfId eq turfId }
                .orderBy(TurfPhotosTable.displayOrder)
                .firstOrNull()?.get(TurfPhotosTable.url)

            val userLat = dto.userLatitude
            val userLon = dto.userLongitude
            val radiusKm = dto.radiusKm
            val distanceKm = if (userLat != null && userLon != null) {
                haversineKm(userLat, userLon, row[TurfsTable.latitude], row[TurfsTable.longitude])
            } else null

            // Filter by radius if provided
            if (distanceKm != null && radiusKm != null && distanceKm > radiusKm) return@map null

            TurfListItemDto(
                id = turfId.toString(),
                name = row[TurfsTable.name],
                city = row[TurfsTable.city],
                address = row[TurfsTable.address],
                latitude = row[TurfsTable.latitude],
                longitude = row[TurfsTable.longitude],
                sports = sports,
                coverPhotoUrl = coverPhoto,
                rating = row[TurfsTable.rating],
                reviewCount = row[TurfsTable.reviewCount],
                minPriceInPaise = minPrice,
                isVerified = row[TurfsTable.isVerified],
                distanceKm = distanceKm
            )
        }.filterNotNull()

        // Sort by distance if location provided, otherwise by rating
        if (dto.userLatitude != null && dto.userLongitude != null) {
            turfs.sortedBy { it.distanceKm ?: Float.MAX_VALUE }
        } else {
            turfs.sortedByDescending { it.rating }
        }.let { sorted ->
            val offset = (dto.page - 1) * dto.pageSize
            sorted.drop(offset).take(dto.pageSize)
        }
    }

    fun updateTurf(turfId: String, ownerId: String, dto: UpdateTurfDto): TurfDetailDto? = transaction {
        val uuid = UUID.fromString(turfId)
        val ownerUuid = UUID.fromString(ownerId)
        val now: Instant = Clock.System.now()

        val updated = TurfsTable.update({
            (TurfsTable.id eq uuid) and (TurfsTable.ownerId eq ownerUuid)
        }) { row ->
            dto.name?.let { row[name] = it }
            dto.description?.let { row[description] = it }
            dto.address?.let { row[address] = it }
            dto.openingTime?.let { row[openingTime] = it }
            dto.closingTime?.let { row[closingTime] = it }
            dto.isActive?.let { row[isActive] = it }
            row[updatedAt] = now
        }

        if (updated == 0) return@transaction null

        dto.amenities?.let { newAmenities ->
            TurfAmenitiesTable.deleteWhere {
                Op.build { TurfAmenitiesTable.turfId eq uuid }
            }
            newAmenities.forEach { amenity ->
                TurfAmenitiesTable.insert {
                    it[TurfAmenitiesTable.turfId] = uuid
                    it[TurfAmenitiesTable.amenity] = amenity
                }
            }
        }

        dto.photoUrls?.let { newPhotos ->
            TurfPhotosTable.deleteWhere {
                Op.build { TurfPhotosTable.turfId eq uuid }
            }
            newPhotos.forEachIndexed { index, url ->
                TurfPhotosTable.insert {
                    it[TurfPhotosTable.turfId] = uuid
                    it[TurfPhotosTable.url] = url
                    it[TurfPhotosTable.displayOrder] = index
                }
            }
        }

        dto.slotPricing?.let { newPricing ->
            newPricing.forEach { pricing ->
                if (!KnownSports.isKnown(pricing.sport.uppercase())) {
                    throw IllegalArgumentException("Unknown sport: ${pricing.sport}")
                }
            }
            TurfSlotPricingTable.deleteWhere {
                Op.build { TurfSlotPricingTable.turfId eq uuid }
            }
            TurfSportsTable.deleteWhere {
                Op.build { TurfSportsTable.turfId eq uuid }
            }
            newPricing.map { it.sport.uppercase() }.distinct().forEach { sport ->
                TurfSportsTable.insert {
                    it[TurfSportsTable.turfId] = uuid
                    it[TurfSportsTable.sport] = sport
                }
            }
            newPricing.forEach { pricing ->
                TurfSlotPricingTable.insert {
                    it[TurfSlotPricingTable.turfId] = uuid
                    it[TurfSlotPricingTable.sport] = pricing.sport.uppercase()
                    it[TurfSlotPricingTable.durationMinutes] = pricing.durationMinutes
                    it[TurfSlotPricingTable.priceInPaise] = pricing.priceInPaise
                }
            }
        }

        getTurfDetail(turfId)
    }

    fun isTurfOwner(turfId: String, userId: String): Boolean = transaction {
        TurfsTable
            .selectAll()
            .where {
                (TurfsTable.id eq UUID.fromString(turfId)) and
                (TurfsTable.ownerId eq UUID.fromString(userId))
            }
            .count() > 0
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private fun getTurfDetail(turfId: String): TurfDetailDto? {
        val uuid = UUID.fromString(turfId)
        val row = TurfsTable.selectAll().where { TurfsTable.id eq uuid }.singleOrNull() ?: return null

        val sports = TurfSportsTable
            .selectAll().where { TurfSportsTable.turfId eq uuid }
            .map { it[TurfSportsTable.sport] }

        val amenities = TurfAmenitiesTable
            .selectAll().where { TurfAmenitiesTable.turfId eq uuid }
            .map { it[TurfAmenitiesTable.amenity] }

        val photos = TurfPhotosTable
            .selectAll().where { TurfPhotosTable.turfId eq uuid }
            .orderBy(TurfPhotosTable.displayOrder)
            .map { it[TurfPhotosTable.url] }

        val durations = TurfSlotPricingTable
            .selectAll().where { TurfSlotPricingTable.turfId eq uuid }
            .map { it[TurfSlotPricingTable.durationMinutes] }
            .distinct()

        val externalRow = TurfExternalSystemTable.selectAll()
            .where { TurfExternalSystemTable.turfId eq uuid }
            .singleOrNull()

        return TurfDetailDto(
            id = turfId,
            name = row[TurfsTable.name],
            description = row[TurfsTable.description],
            address = row[TurfsTable.address],
            city = row[TurfsTable.city],
            latitude = row[TurfsTable.latitude],
            longitude = row[TurfsTable.longitude],
            sports = sports,
            amenities = amenities,
            photoUrls = photos,
            openingTime = row[TurfsTable.openingTime],
            closingTime = row[TurfsTable.closingTime],
            slotDurationsMinutes = durations,
            rating = row[TurfsTable.rating],
            reviewCount = row[TurfsTable.reviewCount],
            isVerified = row[TurfsTable.isVerified],
            externalSystemType = externalRow?.get(TurfExternalSystemTable.systemType)
        )
    }
}

/** Haversine great-circle distance in km */
private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return (R * 2 * asin(sqrt(a))).toFloat()
}
