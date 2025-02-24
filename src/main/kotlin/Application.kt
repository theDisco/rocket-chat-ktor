package com.example

import com.example.config.Database
import com.example.config.respondWithError
import com.example.routing.users
import com.example.service.Observability
import com.example.validation.configureValidation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.ktor.v3_0.server.KtorServerTracing
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.semconv.ServiceAttributes
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.postgresql.util.PSQLException

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
    val telemetry = setupServerTelemetry()

    Database.connect(telemetry)
    Database.migrate()

    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondWithError(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
        exception<RequestValidationException> { call, cause ->
            call.respondWithError(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
        exception<ExposedSQLException> { call, cause ->
            val sqlException = cause.cause as? PSQLException
            if (sqlException?.sqlState == "23505") {
                call.respondWithError(HttpStatusCode.Conflict, "Unique constraint violation")
            } else {
                call.respondWithError(HttpStatusCode.InternalServerError, "Database error")
            }
        }
    }

    configureValidation()

    routing {
        users(Observability(telemetry))
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}
