package com.mrhuo

import com.mrhuo.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    Database2Api.init()
    embeddedServer(
        factory = Netty,
        port = Database2Api.getApiPort(),
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureFreeMarker()
    configureRouting()
}
