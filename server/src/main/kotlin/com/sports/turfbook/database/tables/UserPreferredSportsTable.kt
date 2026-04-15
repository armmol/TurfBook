package com.sports.turfbook.database.tables

import com.sports.turfbook.domain.enums.SportType
import org.jetbrains.exposed.sql.Table

object UserPreferredSportsTable : Table("user_preferred_sports") {
    val userId = reference("user_id", UsersTable)
    val sport = enumerationByName<SportType>("sport", 20)

    override val primaryKey = PrimaryKey(userId, sport)
}
