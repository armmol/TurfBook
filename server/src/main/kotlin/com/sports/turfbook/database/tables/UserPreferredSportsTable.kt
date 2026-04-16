package com.sports.turfbook.database.tables

import org.jetbrains.exposed.sql.Table

object UserPreferredSportsTable : Table("user_preferred_sports") {
    // Plain uuid column (not EntityID-wrapped) so eq(UUID) resolves cleanly in queries
    val userId = uuid("user_id").references(UsersTable.id)
    /** Sport code, e.g. "FOOTBALL". Stored as varchar for extensibility. */
    val sport = varchar("sport", 50)

    override val primaryKey = PrimaryKey(userId, sport)
}
