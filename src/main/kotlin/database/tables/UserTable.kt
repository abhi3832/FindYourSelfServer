package com.example.database.tables

import org.jetbrains.exposed.sql.Table

@kotlinx.serialization.Serializable
data class User(
    val uid: String,
    val name: String,
    val phone: String,
    val profileImageUrl: String? = null,
    val aboutYourself: String? = null,
    val username: String,
    val gender: String,
    val age: Int,
    val isOnline: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val pushToken: String? = null
)


object Users : Table("users") {
    val uid = varchar("uid", 64)
    val name = varchar("name", 50)
    val phone = varchar("phone", 15).uniqueIndex()
    val profileImageUrl = varchar("profile_image_url", 512).nullable()
    val bio = text("bio").nullable()
    val username = varchar("username", 50).uniqueIndex()
    val gender = varchar("gender", 10)
    val age = integer("age")
    val isOnline = bool("is_online").default(false)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    val pushToken = varchar("push_token", 256).nullable()

    override val primaryKey = PrimaryKey(uid)
}
