package com.example.database.repository

import com.example.connectRoutes.activeSockets
import com.example.database.RedisProvider
import com.example.database.tables.StrangerChat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object ConnectRepo {

    suspend fun registerUserInInterests(uid: String, interests: List<String>) {
        withContext(Dispatchers.IO) {
            interests.forEach { interest ->
                RedisProvider.syncCommands.sadd("interest:$interest", uid)
            }
            RedisProvider.syncCommands.sadd("user:$uid", *interests.toTypedArray())
            RedisProvider.syncCommands.expire("user:$uid", 120) // Optional: auto expire in 2 mins
        }
    }

    suspend fun findBestMatch(uid: String): String? = withContext(Dispatchers.IO) {
        val interests = RedisProvider.syncCommands.smembers("user:$uid")
        val potentialUsers = mutableSetOf<String>()

        interests.forEach { interest ->
            val users = RedisProvider.syncCommands.smembers("interest:$interest")
            potentialUsers.addAll(users)
        }

        potentialUsers.remove(uid) // Avoid self-match

        var bestUser: String? = null
        var maxMatch = 0

        for (otherUser in potentialUsers) {
            // 🔒 Skip if already matched
            val isAlreadyMatched = RedisProvider.syncCommands.get("matched:$otherUser") != null
            // 🔌 Skip if not connected via WebSocket
            val isOnline = activeSockets.containsKey(otherUser)

            if (isAlreadyMatched || !isOnline) continue

            val otherInterests = RedisProvider.syncCommands.smembers("user:$otherUser")
            val common = interests.intersect(otherInterests)

            if (common.size > maxMatch) {
                maxMatch = common.size
                bestUser = otherUser
            }
        }

        bestUser
    }


    suspend fun createChatBetween(userA: String, userB: String): String = withContext(Dispatchers.IO) {
        val chatId = UUID.randomUUID().toString()
        transaction {
            StrangerChat.insert {
                it[chatUId] = chatId
                it[userOne] = userA
                it[userTwo] = userB
            }
        }
        chatId
    }

    suspend fun deleteChat(chatId: String) = withContext(Dispatchers.IO) {
        transaction {
            StrangerChat.deleteWhere { StrangerChat.chatUId eq chatId }
        }
    }
}
