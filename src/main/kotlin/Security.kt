package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.github.cdimascio.dotenv.dotenv

fun Application.configureSecurity() {

    val dotenv = dotenv()

    val jwtSecret = dotenv["JWT_SECRET"] ?: "secret"
    val jwtIssuer = dotenv["JWT_ISSUER"] ?: "find_your_self_app"
    val jwtAudience = dotenv["JWT_AUDIENCE"] ?: "audience"
    val jwtRealm = dotenv["JWT_REALM"] ?: "realm"

    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
