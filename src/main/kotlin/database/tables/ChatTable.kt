package com.example.database.tables

import org.jetbrains.exposed.sql.Table

object StrangerChat : Table("strangerChat") {
    val chatUId = varchar("chatId", 64).uniqueIndex()
    val userOne = varchar("userOne", 64)
    val userTwo = varchar("userTwo", 64)
    val createdAt = long("createdAt")
    override val primaryKey = PrimaryKey(chatUId)
}
