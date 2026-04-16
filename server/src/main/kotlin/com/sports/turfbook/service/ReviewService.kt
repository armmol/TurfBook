package com.sports.turfbook.service

import com.sports.turfbook.api.dto.review.CreateReviewDto
import com.sports.turfbook.api.dto.review.ReviewDto
import com.sports.turfbook.database.tables.BookingsTable
import com.sports.turfbook.database.tables.ReviewsTable
import com.sports.turfbook.database.tables.TurfsTable
import com.sports.turfbook.database.tables.UsersTable
import com.sports.turfbook.domain.enums.BookingStatus
import com.sports.turfbook.plugins.ConflictException
import com.sports.turfbook.plugins.NotFoundException
import com.sports.turfbook.plugins.UnauthorizedException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ReviewService {

    /**
     * Creates a review for a turf. The booking must:
     *  - belong to the requesting user
     *  - be for the specified turf
     *  - have status CONFIRMED
     *  - not already have a review
     *
     * Updates the turf's average rating and review count atomically.
     */
    fun createReview(userId: String, turfId: String, dto: CreateReviewDto): ReviewDto = transaction {
        if (dto.rating !in 1..5) throw IllegalArgumentException("Rating must be between 1 and 5")

        val bookingUuid = UUID.fromString(dto.bookingId)
        val turfUuid = UUID.fromString(turfId)
        val userUuid = UUID.fromString(userId)

        // Validate the booking
        val bookingRow = BookingsTable.selectAll()
            .where { BookingsTable.id eq bookingUuid }
            .singleOrNull() ?: throw NotFoundException("Booking not found")

        if (bookingRow[BookingsTable.userId] != userUuid) {
            throw UnauthorizedException("Not your booking")
        }
        if (bookingRow[BookingsTable.turfId] != turfUuid) {
            throw IllegalArgumentException("Booking is not for this turf")
        }
        if (bookingRow[BookingsTable.status] != BookingStatus.CONFIRMED) {
            throw IllegalArgumentException("You can only review confirmed bookings")
        }

        // Check for duplicate review
        val existing = ReviewsTable.selectAll()
            .where { ReviewsTable.bookingId eq bookingUuid }
            .count() > 0
        if (existing) throw ConflictException("You have already reviewed this booking")

        val now: Instant = Clock.System.now()
        val reviewId = UUID.randomUUID()

        ReviewsTable.insert {
            it[id] = reviewId
            it[ReviewsTable.userId] = userUuid
            it[ReviewsTable.turfId] = turfUuid
            it[ReviewsTable.bookingId] = bookingUuid
            it[rating] = dto.rating
            it[comment] = dto.comment
            it[createdAt] = now
        }

        // Recompute turf average rating
        val allRatings = ReviewsTable.selectAll()
            .where { ReviewsTable.turfId eq turfUuid }
            .map { it[ReviewsTable.rating] }

        val newAvg = allRatings.average().toFloat()
        TurfsTable.update({ TurfsTable.id eq turfUuid }) {
            it[rating] = newAvg
            it[reviewCount] = allRatings.size
        }

        // Fetch user details for the response
        val userRow = UsersTable.selectAll()
            .where { UsersTable.id eq userUuid }
            .single()

        ReviewDto(
            id = reviewId.toString(),
            userId = userId,
            userName = userRow[UsersTable.name] ?: "Anonymous",
            userPhotoUrl = userRow[UsersTable.profilePhotoUrl],
            turfId = turfId,
            rating = dto.rating,
            comment = dto.comment,
            createdAt = now.toString()
        )
    }

    fun getTurfReviews(turfId: String, page: Int = 1, pageSize: Int = 20): List<ReviewDto> = transaction {
        val turfUuid = UUID.fromString(turfId)
        val offset = ((page - 1) * pageSize).toLong()

        ReviewsTable
            .join(UsersTable, JoinType.LEFT, ReviewsTable.userId, UsersTable.id)
            .selectAll()
            .where { ReviewsTable.turfId eq turfUuid }
            .orderBy(ReviewsTable.createdAt, SortOrder.DESC)
            .limit(pageSize, offset)
            .map { row ->
                ReviewDto(
                    id = row[ReviewsTable.id].toString(),
                    userId = row[ReviewsTable.userId].toString(),
                    userName = row[UsersTable.name] ?: "Anonymous",
                    userPhotoUrl = row[UsersTable.profilePhotoUrl],
                    turfId = turfId,
                    rating = row[ReviewsTable.rating],
                    comment = row[ReviewsTable.comment],
                    createdAt = row[ReviewsTable.createdAt].toString()
                )
            }
    }

    fun getUserReview(userId: String, bookingId: String): ReviewDto? = transaction {
        ReviewsTable
            .join(UsersTable, JoinType.LEFT, ReviewsTable.userId, UsersTable.id)
            .selectAll()
            .where {
                (ReviewsTable.bookingId eq UUID.fromString(bookingId)) and
                (ReviewsTable.userId eq UUID.fromString(userId))
            }
            .singleOrNull()
            ?.let { row ->
                ReviewDto(
                    id = row[ReviewsTable.id].toString(),
                    userId = userId,
                    userName = row[UsersTable.name] ?: "Anonymous",
                    userPhotoUrl = row[UsersTable.profilePhotoUrl],
                    turfId = row[ReviewsTable.turfId].toString(),
                    rating = row[ReviewsTable.rating],
                    comment = row[ReviewsTable.comment],
                    createdAt = row[ReviewsTable.createdAt].toString()
                )
            }
    }
}
