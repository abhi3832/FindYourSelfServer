package com.example.database.repository

import com.example.database.tables.User
import com.example.database.tables.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun addUser(user: User): Boolean {
    return try {
        transaction {
            Users.insert {
                it[uid] = user.uid
                it[name] = user.name
                it[phone] = user.phone
                it[profileImageUrl] = user.profileImageUrl
                it[bio] = user.aboutYourself
                it[username] = user.username
                it[gender] = user.gender
                it[age] = user.age
                it[isOnline] = false
                it[createdAt] = System.currentTimeMillis()
                it[updatedAt] = System.currentTimeMillis()
                it[pushToken] = user.pushToken
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


fun getUserByUid(uid: String): User? {
    return try {
        transaction {
            Users.select { Users.uid eq uid }
                .map {
                    User(
                        uid = it[Users.uid],
                        name = it[Users.name],
                        phone = it[Users.phone],
                        profileImageUrl = it[Users.profileImageUrl],
                        aboutYourself = it[Users.bio],
                        username = it[Users.username],
                        gender = it[Users.gender] ,
                        age = it[Users.age] ,
                        isOnline = it[Users.isOnline],
                        createdAt = it[Users.createdAt],
                        updatedAt = it[Users.updatedAt],
                        pushToken = it[Users.pushToken]
                    )
                }
                .singleOrNull()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getUserByPhone(phone: String): User? {
    return try {
        transaction {
            Users.select { Users.phone eq phone }
                .map {
                    User(
                        uid = it[Users.uid],
                        name = it[Users.name],
                        phone = it[Users.phone],
                        profileImageUrl = it[Users.profileImageUrl],
                        aboutYourself = it[Users.bio],
                        username = it[Users.username],
                        gender = it[Users.gender] ,
                        age = it[Users.age] ,
                        isOnline = it[Users.isOnline],
                        createdAt = it[Users.createdAt],
                        updatedAt = it[Users.updatedAt],
                        pushToken = it[Users.pushToken]
                    )
                }
                .singleOrNull()
        }
    } catch (e: Exception) {
        println("Exception in getUserByPhone: $e")
        e.printStackTrace()
        null
    }
}

fun getUserByUsername(username: String): User? {
    return try {
        transaction {
            Users.select { Users.username eq username }
                .map {
                    User(
                        uid = it[Users.uid],
                        name = it[Users.name],
                        phone = it[Users.phone],
                        profileImageUrl = it[Users.profileImageUrl],
                        aboutYourself = it[Users.bio],
                        username = it[Users.username],
                        gender = it[Users.gender],
                        age = it[Users.age],
                        isOnline = it[Users.isOnline],
                        createdAt = it[Users.createdAt],
                        updatedAt = it[Users.updatedAt],
                        pushToken = it[Users.pushToken]
                    )
                }
                .singleOrNull()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


