package com.example.database.redisOperations

import com.example.database.RedisProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun saveOtp(phone: String, otp: String) {
    withContext(Dispatchers.IO) {
        RedisProvider.syncCommands.setex("otp:$phone", 300, otp)
    }
}

suspend fun getOtp(phone: String): String? {
    return withContext(Dispatchers.IO) {
        RedisProvider.syncCommands.get("otp:$phone")
    }
}

suspend fun deleteOtp(phone: String) {
    withContext(Dispatchers.IO) {
        RedisProvider.syncCommands.del("otp:$phone")
    }
}


