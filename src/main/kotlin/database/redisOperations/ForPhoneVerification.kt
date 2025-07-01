package com.example.database.redisOperations

import com.example.database.RedisProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


suspend fun markPhoneVerified(phone: String) {
    withContext(Dispatchers.IO) {
        RedisProvider.syncCommands.set("verified:$phone", "true")
    }
}

suspend fun isPhoneVerified(phone: String): Boolean {
    return withContext(Dispatchers.IO) {
        RedisProvider.syncCommands.get("verified:$phone") == "true"
    }
}

suspend fun removePhoneVerification(phone: String) {
    withContext(Dispatchers.IO) {
        RedisProvider.syncCommands.del("verified:$phone")
    }
}