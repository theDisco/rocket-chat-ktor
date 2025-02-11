package com.example

import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.server.KtorServerTracing
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.semconv.ServiceAttributes

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.setupServerTelemetry(): OpenTelemetry {
    val openTelemetry = AutoConfiguredOpenTelemetrySdk.builder().addResourceCustomizer { oldResource, _ ->
        oldResource.toBuilder()
            .putAll(oldResource.attributes)
            .put(ServiceAttributes.SERVICE_NAME, "rocket-chat")
            .build()
    }.build().openTelemetrySdk

    install(KtorServerTracing) {
        setOpenTelemetry(openTelemetry)
    }

    return openTelemetry
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    configureRouting()
}
