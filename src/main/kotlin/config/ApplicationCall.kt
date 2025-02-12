package com.example.config

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ErrorResponse(val message: String)

suspend fun ApplicationCall.respondWithError(status: HttpStatusCode, message: String) {
    val jsonResponse = Json.encodeToString(ErrorResponse.serializer(), ErrorResponse(message))
    respondText(jsonResponse, status = status, contentType = ContentType.Application.Json)
}