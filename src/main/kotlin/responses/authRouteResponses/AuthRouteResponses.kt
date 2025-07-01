package com.example.responses.authRouteResponses

import com.example.database.tables.User

@kotlinx.serialization.Serializable
data class UsernameResponse(val exists: Boolean)

@kotlinx.serialization.Serializable
data class SendOtpResponse(
    val success: Boolean,
    val message: String,
    val isNewUser: Boolean
)

@kotlinx.serialization.Serializable
data class VerifyOtpResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val user: User?,
    val isNewUser: Boolean,
    val isOtpVerified : Boolean
)

@kotlinx.serialization.Serializable
data class SignUpRequest(
    val fullName: String,
    val username: String,
    val phoneNumber: String,
    val gender: String,
    val profilePhotoUrl: String? = null,
    val age: Int,
    val about: String
)

@kotlinx.serialization.Serializable
data class SignUpResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val user: User?,
    val isOtpVerified : Boolean
)

@kotlinx.serialization.Serializable
data class TokenValidityResponse(
    val isTokenValid : Boolean,
    val message : String? = null,
)



@kotlinx.serialization.Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@kotlinx.serialization.Serializable
data class TokenResponse(
    val accessToken: String?,
    val refreshToken: String?
)
