package com.example

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.plugins.swagger.*
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.logs.Severity
import io.opentelemetry.api.trace.Span
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class MessageRequest(
    val content: String,
    val author: String
)

@Serializable
data class MessageResponse(
    val id: String,
    val content: String,
    val author: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun Application.configureRouting() {
    val openTelemetry: OpenTelemetry = setupServerTelemetry()
    val tracer = openTelemetry.getTracer("rocket-chat")
    val meter = openTelemetry.getMeter("rocket-chat")
    val logger = openTelemetry.logsBridge.get("rocket-chat")

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        
        post("/message") {
            val span = tracer.spanBuilder("create-message").startSpan()
            try {
                span.makeCurrent().use {
                    val id = java.util.UUID.randomUUID().toString()

                    meter.counterBuilder("message.create")
                        .setUnit("{request}")
                        .setDescription("Counter for message creation")
                        .build()
                        .add(1)
                    logger.logRecordBuilder()
                        .setBody("Creating a message")
                        .setSeverity(Severity.INFO)
                        .setSeverityText("INFO")
                        .setAttribute(AttributeKey.stringKey("id"), id)
                        .emit()

                    val request = call.receive<MessageRequest>()

                    val contentAttr = Attributes.of(AttributeKey.stringKey("content"), request.content)
                    Span.current().addEvent("Starting the work", contentAttr, Instant.now())

                    val response = MessageResponse(
                        id = id,
                        content = request.content,
                        author = request.author
                    )
                    call.respond(HttpStatusCode.Created, response)

                    val authorAttr = Attributes.of(AttributeKey.stringKey("author"), request.author)
                    Span.current().addEvent("Finishing the work", authorAttr, Instant.now())
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Failed to create message: ${e.message}")
            } finally {
                span.end()
            }
        }

        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
    }
}
