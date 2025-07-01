package com.example

import com.example.database.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    DatabaseFactory.init()
    install(WebSockets)
    configureSerialization()
    configureMonitoring()
    configureSecurity()
    configureRouting()
}
