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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.time.LocalDateTime

fun Route.users() {
    route("/users") {
        get {
            val response = Database.runQuery {
                Users.selectAll().map { User.fromRow(it) }
            }
            call.respond(response)
        }

        post {
            val request = call.receive<UserRequest>()
            val user = Database.runQuery {
                val userId = Users.insertAndGetId {
                    it[name] = request.name
                    it[email] = request.email
                    it[createdAt] = LocalDateTime.now()
                    it[updatedAt] = LocalDateTime.now()
                }
                Users.selectAll()
                    .where(Users.id eq userId.value)
                    .map { User.fromRow(it) }
                    .singleOrNull()
            }

            if (user != null) {
                call.respond(HttpStatusCode.Created, user)
            } else {
                call.respond(HttpStatusCode.BadRequest, "Failed to create user")
            }
        }
    }
}

//    val openTelemetry: OpenTelemetry = setupServerTelemetry()
//    val tracer = openTelemetry.getTracer("rocket-chat")
//    val meter = openTelemetry.getMeter("rocket-chat")
//    val logger = openTelemetry.logsBridge.get("rocket-chat")

//    routing {
//        get("/") {
//            call.respondText("Hello World!")
//        }
//
//        post("/message") {
//            val span = tracer.spanBuilder("create-message").startSpan()
//            try {
//                span.makeCurrent().use {
//                    val id = java.util.UUID.randomUUID().toString()
//
//                    meter.counterBuilder("message.create")
//                        .setUnit("{request}")
//                        .setDescription("Counter for message creation")
//                        .build()
//                        .add(1)
//                    logger.logRecordBuilder()
//                        .setBody("Creating a message")
//                        .setSeverity(Severity.INFO)
//                        .setSeverityText("INFO")
//                        .setAttribute(AttributeKey.stringKey("id"), id)
//                        .emit()
//
//                    val request = call.receive<MessageRequest>()
//
//                    val contentAttr = Attributes.of(AttributeKey.stringKey("content"), request.content)
//                    Span.current().addEvent("Starting the work", contentAttr, Instant.now())
//
//                    val response = MessageResponse(
//                        id = id,
//                        content = request.content,
//                        author = request.author
//                    )
//                    call.respond(HttpStatusCode.Created, response)
//
//                    val authorAttr = Attributes.of(AttributeKey.stringKey("author"), request.author)
//                    Span.current().addEvent("Finishing the work", authorAttr, Instant.now())
//                }
//            } catch (e: Exception) {
//                call.respond(HttpStatusCode.BadRequest, "Failed to create message: ${e.message}")
//            } finally {
//                span.end()
//            }
//        }
//    }
