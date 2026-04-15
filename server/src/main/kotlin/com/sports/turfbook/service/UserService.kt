package com.sports.turfbook.service

import com.sports.turfbook.api.dto.user.UpdateProfileDto
import com.sports.turfbook.api.dto.user.UserDto
import com.sports.turfbook.database.tables.UserPreferredSportsTable
import com.sports.turfbook.database.tables.UsersTable
import com.sports.turfbook.domain.enums.SportType
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class UserService {

    fun findById(userId: String): UserDto? = transaction {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq UUID.fromString(userId) }
            .singleOrNull()
            ?.let { row ->
                val sports = UserPreferredSportsTable
                    .selectAll()
                    .where { UserPreferredSportsTable.userId eq row[UsersTable.id] }
                    .map { it[UserPreferredSportsTable.sport] }

                UserDto(
                    id = row[UsersTable.id].toString(),
                    phone = row[UsersTable.phone],
                    name = row[UsersTable.name] ?: "",
                    email = row[UsersTable.email],
                    profilePhotoUrl = row[UsersTable.profilePhotoUrl],
                    city = row[UsersTable.city] ?: "",
                    preferredSports = sports,
                    totalBookings = row[UsersTable.totalBookings]
                )
            }
    }

    fun findByPhone(phone: String): UserDto? = transaction {
        UsersTable
            .selectAll()
            .where { UsersTable.phone eq phone }
            .singleOrNull()
            ?.let { row ->
                val sports = UserPreferredSportsTable
                    .selectAll()
                    .where { UserPreferredSportsTable.userId eq row[UsersTable.id] }
                    .map { it[UserPreferredSportsTable.sport] }

                UserDto(
                    id = row[UsersTable.id].toString(),
                    phone = row[UsersTable.phone],
                    name = row[UsersTable.name] ?: "",
                    email = row[UsersTable.email],
                    profilePhotoUrl = row[UsersTable.profilePhotoUrl],
                    city = row[UsersTable.city] ?: "",
                    preferredSports = sports,
                    totalBookings = row[UsersTable.totalBookings]
                )
            }
    }

    /** Creates a new user on first OTP verification */
    fun createUser(phone: String): UserDto = transaction {
        val now = Clock.System.now().toJavaInstant()
        val newId = UUID.randomUUID()

        UsersTable.insert {
            it[id] = newId
            it[UsersTable.phone] = phone
            it[createdAt] = now
            it[updatedAt] = now
        }

        UserDto(
            id = newId.toString(),
            phone = phone,
            name = "",
            city = ""
        )
    }

    fun updateProfile(userId: String, dto: UpdateProfileDto): UserDto? = transaction {
        val now = Clock.System.now().toJavaInstant()
        val uuid = UUID.fromString(userId)

        UsersTable.update({ UsersTable.id eq uuid }) { row ->
            dto.name?.let { row[name] = it }
            dto.email?.let { row[email] = it }
            dto.city?.let { row[city] = it }
            dto.profilePhotoUrl?.let { row[profilePhotoUrl] = it }
            row[updatedAt] = now
        }

        dto.preferredSports?.let { newSports ->
            UserPreferredSportsTable.deleteWhere { userId eq uuid }
            newSports.forEach { sport ->
                UserPreferredSportsTable.insert {
                    it[UserPreferredSportsTable.userId] = uuid
                    it[UserPreferredSportsTable.sport] = sport
                }
            }
        }

        findById(userId)
    }
}
