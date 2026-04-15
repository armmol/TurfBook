package com.sports.turfbook.database.tables

import com.sports.turfbook.domain.enums.SportType
import org.jetbrains.exposed.sql.Table

object UserPreferredSportsTable : Table("user_preferred_sports") {
    // Plain uuid column (not EntityID-wrapped) so eq(UUID) resolves cleanly in queries
    val userId = uuid("user_id").references(UsersTable.id)
    val sport = enumerationByName<SportType>("sport", 20)

    override val primaryKey = PrimaryKey(userId, sport)
}
