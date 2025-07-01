package com.example.authRoutes

import com.auth0.jwt.exceptions.JWTVerificationException
import com.example.database.redisOperations.getOtp
import com.example.database.redisOperations.isPhoneVerified
import com.example.database.redisOperations.markPhoneVerified
import com.example.database.redisOperations.removePhoneVerification
import com.example.database.redisOperations.saveOtp
import com.example.database.repository.addUser
import com.example.database.repository.getUserByPhone
import com.example.database.tables.User
import com.example.database.tables.Users
import com.example.jwt.JwtConfig
import com.example.jwt.JwtConfig.generateAccessToken
import com.example.jwt.JwtConfig.generateRefreshToken
import com.example.responses.authRouteResponses.RefreshTokenRequest
import com.example.responses.authRouteResponses.SendOtpResponse
import com.example.responses.authRouteResponses.SignUpRequest
import com.example.responses.authRouteResponses.SignUpResponse
import com.example.responses.authRouteResponses.TokenResponse
import com.example.responses.authRouteResponses.TokenValidityResponse
import com.example.responses.authRouteResponses.UsernameResponse
import com.example.responses.authRouteResponses.VerifyOtpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.request.receiveNullable
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID


fun Route.authRoutes() {

    post("/sendOtp") {
        val phone = call.request.queryParameters["phone"]

        if(phone.isNullOrBlank()){
            call.respond(HttpStatusCode.BadRequest, SendOtpResponse(false, "Missing phone", false))
            return@post
        }

        val otp = (100000..999999).random().toString()
        //otpStore[phone] = otp
        saveOtp(phone, otp)

        println("Sending OTP to $phone: $otp")

        val isNewUser = transaction {
            Users.select { Users.phone eq phone }
                .empty() // true if no such user
        }

        call.respond(HttpStatusCode.OK, SendOtpResponse(true, "Otp sent successfully", isNewUser))
    }

    post("/verifyOtp") {
        val phone = call.request.queryParameters["phone"]
        val otp = call.request.queryParameters["otp"]

        if (phone.isNullOrBlank() || otp.isNullOrBlank()) {
            println("Blank Phone or Otp")
            call.respond(
                VerifyOtpResponse(
                    refreshToken = null,
                    accessToken = null,
                    user = null,
                    isNewUser = false,
                    isOtpVerified = false
                )
            )
            return@post
        }

        val storedOtp = getOtp(phone)
        if (storedOtp == null || storedOtp != otp) {
            println("UnAuthorized, Current Received OTP : $otp")
            println("UnAuthorized, Current Received Phone : $phone")
            call.respond(VerifyOtpResponse(refreshToken = null, accessToken = null, user = null, isNewUser = false, isOtpVerified = false))
            return@post
        }

        val userExists = transaction {
            Users.select { Users.phone eq phone }
                .empty() // true if no such user
        }

        if (userExists) {
            println("New User, Verified, Proceed To SignUp")
            markPhoneVerified(phone)
            call.respond(VerifyOtpResponse(refreshToken = null, accessToken = null, user = null, isNewUser = true, isOtpVerified = true))
            return@post
        }

        val user = getUserByPhone(phone)

        println(phone)
        println(user)

        println("Old User, Verified, Generating Tokens, Proceed to App")

        val accessToken = generateAccessToken(phone) // You must have implemented this
        val refreshToken = generateRefreshToken(phone)
        call.respond(VerifyOtpResponse(refreshToken = refreshToken, accessToken = accessToken, user = user, isNewUser = false, isOtpVerified = true))
    }

    post("/checkUserName") {
        val userName = call.request.queryParameters["userName"]

        if (userName.isNullOrBlank()) {
            println("Blank UserName")
            call.respond(UsernameResponse(exists = false))
            return@post
        }

        println("Checking if username exists : $userName")
        val usernameTrimmed = userName.trim().lowercase()
        val exists = transaction {
            Users.select { Users.username.lowerCase() eq usernameTrimmed }
                .empty().not()
        }
        call.respond(UsernameResponse(exists = exists))
    }

    post("/signUp") {

        val request = call.receive<SignUpRequest>()

        val isVerified = isPhoneVerified(phone = request.phoneNumber)
        if (!isVerified) {
            println("Phone number not verified via OTP")
            return@post call.respond(HttpStatusCode.Forbidden, "Phone number not verified via OTP")
        }

        val userNameExists = transaction {
            Users.select { Users.username.lowerCase() eq request.username.trim().lowercase() }
                .empty().not()
        }

        if (userNameExists) {
            println("Username already exists")
            return@post call.respond(HttpStatusCode.Conflict, "Username already exists")
        }

        val phoneAlreadyExists = transaction {
            Users.select { Users.phone eq request.phoneNumber }
                .empty().not()
        }

        if (phoneAlreadyExists) {
            println("Phone number already registered")
            return@post call.respond(HttpStatusCode.Conflict, "Phone number already registered")
        }

        println("Creating User")

        val uid = UUID.randomUUID().toString()

        val user = User(
            uid = uid,
            name = request.fullName,
            username = request.username,
            phone = request.phoneNumber,
            gender = request.gender,
            age = request.age,
            aboutYourself = request.about,
            profileImageUrl = request.profilePhotoUrl,
            isOnline = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            pushToken = null
        )

        val success = addUser(user)

        if(!success){
            println("Failed to add user")
            return@post call.respond(HttpStatusCode.InternalServerError, "Failed to add user")
        }

        //otpVerifiedPhones.remove(request.phoneNumber)
        removePhoneVerification(phone = request.phoneNumber)

        val accessToken = generateAccessToken(request.phoneNumber)
        val refreshToken = generateRefreshToken(request.phoneNumber)

        call.respond(
            SignUpResponse(
                refreshToken = refreshToken,
                accessToken = accessToken,
                user = user,
                isOtpVerified = true
            )
        )
    }

    post("/checkTokenValidity") {
        val authHeader = call.request.headers["Authorization"]

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            call.respond(HttpStatusCode.Unauthorized,
                TokenValidityResponse(false, "Invalid or missing Authorization header")
            )
            return@post
        }

        val token = authHeader.removePrefix("Bearer ").trim()

        try {
            val decodedJWT = JwtConfig.verifier().verify(token)

            val phone = decodedJWT.getClaim("phone").asString()

            if (phone.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, TokenValidityResponse(false, "Invalid token: Phone number is missing"))
                return@post
            }

            val user = getUserByPhone(phone)

            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized,  TokenValidityResponse(false, "Invalid token: User not found"))
                return@post
            }

            // ✅ Token is valid
            call.respond(HttpStatusCode.OK, TokenValidityResponse(true,"Token is Valid"))

        } catch (e: JWTVerificationException) {
            call.respond(HttpStatusCode.Unauthorized, TokenValidityResponse(false, "Invalid token: ${e.message}"))
        }
    }

    post("/refreshTokens") {
        val refreshToken = call.receiveNullable<RefreshTokenRequest>()?.refreshToken

        if (refreshToken.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, TokenResponse(null, null))
            return@post
        }

        try {
            val decoded = JwtConfig.verifier().verify(refreshToken)
            val phone = decoded.getClaim("phone").asString()

            if (phone.isNullOrBlank()) {
                call.respond(HttpStatusCode.Unauthorized, TokenResponse(null, null))
                return@post
            }

            val user = getUserByPhone(phone)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, TokenResponse(null, null))
                return@post
            }

            val newAccessToken = generateAccessToken(phone)
            val newRefreshToken = generateRefreshToken(phone)


            call.respond(HttpStatusCode.OK, TokenResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken
            ))

        } catch (e: JWTVerificationException) {
            call.respond(HttpStatusCode.Unauthorized, TokenResponse(null, null))
        }
    }

}