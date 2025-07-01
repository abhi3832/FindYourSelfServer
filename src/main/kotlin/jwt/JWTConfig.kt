package com.example.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.github.cdimascio.dotenv.dotenv
import java.util.Date

object JwtConfig {
    private val secret = System.getenv("JWT_SECRET") ?: "secret"
    private val issuer = System.getenv("JWT_ISSUER") ?: "find_your_self_app"
    private val audience = System.getenv("JWT_AUDIENCE") ?: "audience"
    private const val accessTokenExpiry = 60 * 60 * 1000L // 60 minutes
    private const val refreshTokenExpiry = 365 * 24 * 60 * 60 * 1000L //  1 year

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateAccessToken(phone: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("phone", phone)
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiry))
            .sign(algorithm)

    fun generateRefreshToken(phone: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("phone", phone)
            .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenExpiry))
            .sign(algorithm)

    fun verifier(): JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
}
