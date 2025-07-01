package com.example.connectRoutes

import com.example.database.RedisProvider
import com.example.database.repository.ConnectRepo.createChatBetween
import com.example.database.repository.ConnectRepo.findBestMatch
import com.example.database.repository.ConnectRepo.registerUserInInterests
import com.example.database.repository.getUserByUid
import com.example.responses.connectUserRespobseAndRequests.ConnectUserRequests
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import com.example.responses.connectUserRespobseAndRequests.ConnectUserResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import java.util.UUID


val activeSockets = mutableMapOf<String, DefaultWebSocketServerSession>()
fun Route.connectRoute(){


    webSocket("/connect") {
        val params = call.request.queryParameters
        val uid = params["uid"] ?: return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing UID"))
        val interests = params["interests"]?.split(",") ?: return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing interests"))

        println("[$uid] Connected with interests: $interests")

        activeSockets[uid] = this

        withContext(Dispatchers.IO) {
            registerUserInInterests(uid, interests)
        }

        val match = withContext(Dispatchers.IO) { findBestMatch(uid) }

        if (match != null && activeSockets.containsKey(match)) {
            val chatId = generateDeterministicChatId(uid, match)

            RedisProvider.syncCommands.setex("matched:$uid", 300, "$chatId:$match")
            RedisProvider.syncCommands.setex("matched:$match", 300, "$chatId:$uid")

            withContext(Dispatchers.IO) {
                cleanupUser(uid)
                cleanupUser(match)
            }

            val responseToUid = ConnectUserResponse(true, chatId, matchedUser = match)
            val responseToMatch = ConnectUserResponse(true, chatId, matchedUser = uid)

            val jsonToUid = Json.encodeToString(responseToUid)
            val jsonToMatch = Json.encodeToString(responseToMatch)

            activeSockets[uid]?.send(Frame.Text(jsonToUid))
            activeSockets[match]?.send(Frame.Text(jsonToMatch))

            activeSockets.remove(uid)
            activeSockets.remove(match)
            return@webSocket
        }

        // ✅ Timeout Logic Starts Here
        try {
            withTimeout(60_000) {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        println("[$uid] Ping: $text")

                        val stored = RedisProvider.syncCommands.get("matched:$uid")
                        if (stored != null) {
                            val parts = stored.split(":")
                            val chatId = parts[0]
                            val matchedUser = parts[1]

                            val response = ConnectUserResponse(true, chatId, matchedUser)
                            val jsonResponse = Json.encodeToString(response)
                            send(Frame.Text(jsonResponse))

                            activeSockets.remove(uid)
                            break
                        }
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            println("[$uid] Timeout - no match found")
            send(Frame.Text("{\"timeout\": true}"))
            close(CloseReason(CloseReason.Codes.NORMAL, "Timeout - no match"))
        } finally {
            activeSockets.remove(uid)
            cleanupUser(uid)
        }
    }


    get("/userDetails"){
        val userId = call.request.queryParameters["userId"]
        if(userId.isNullOrBlank()){
            call.respond(HttpStatusCode.BadRequest, "Missing userId")
            return@get
        }

        val user = getUserByUid(userId)

        if(user != null){
            call.respond(HttpStatusCode.OK, user)
        }else{
            call.respond(HttpStatusCode.NotFound, "User not found")
        }
    }

}

fun generateDeterministicChatId(user1: String, user2: String): String {
    return if (user1 < user2) "${user1}_$user2" else "${user2}_$user1"
}

suspend fun cleanupUser(uid: String) {
    withContext(Dispatchers.IO) {
        val interests = RedisProvider.syncCommands.smembers("user:$uid")
        interests.forEach { interest ->
            RedisProvider.syncCommands.srem("interest:$interest", uid)
        }
        RedisProvider.syncCommands.del("user:$uid", "matched:$uid")
    }
}

