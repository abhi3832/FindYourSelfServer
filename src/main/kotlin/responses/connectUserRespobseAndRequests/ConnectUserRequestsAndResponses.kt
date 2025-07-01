package com.example.responses.connectUserRespobseAndRequests

@kotlinx.serialization.Serializable
data class ConnectUserRequests(
    val uid : String,
    val interests : List<String>
)

@kotlinx.serialization.Serializable
data class ConnectUserResponse(
    val isConnected : Boolean,
    val chatId : String? = null,
    val matchedUser : String? = null
)