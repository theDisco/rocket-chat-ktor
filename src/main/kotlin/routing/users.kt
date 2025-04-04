package com.example.routing

import com.example.config.Database
import com.example.config.respondWithError
import com.example.model.CreateUserRequest
import com.example.model.UpdateUserRequest
import com.example.model.User
import com.example.model.Users
import com.example.service.Observability
import com.example.service.increment
import com.example.service.info
import com.example.service.span
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime

fun Route.users(observability: Observability) {
    route("/users") {
        get {
            observability.meter.increment("user.list", "{request}", description = "List users")
            val response = Database.runQuery {
                Users.selectAll().limit(10).map { User.fromRow(it) }
            }
            observability.logger.info(
                "Retrieving all users",
                Attributes.of(AttributeKey.stringKey("some_important_context_detail"), "foo")
            )
            call.respond(response)
        }

        post {
            observability.meter.increment("user.create", "{request}", description = "Create user")
            val request = call.receive<CreateUserRequest>()
            observability.logger.info(
                "creating a user",
                Attributes.of(AttributeKey.stringKey("name"), request.name)
            )

            val user =
                observability.tracer.span("CreateUserDatabaseOperation") {
                    Database.runQuery {
                        val existingUser = Users.selectAll()
                            .where(Users.email eq request.email)
                            .singleOrNull()

                        if (existingUser != null) {
                            throw IllegalArgumentException("A user with this email already exists.")
                        }

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
                }
            if (user != null) {
                call.respond(HttpStatusCode.Created, user)
            } else {
                call.respondWithError(HttpStatusCode.BadRequest, "Failed to create user")
            }
        }

        get("/{id}") {
            observability.meter.increment("user.get", "{request}", description = "Get user")
            val id = call.parameters["id"]?.toIntOrNull()
            val user = Database.runQuery {
                Users.selectAll()
                    .where(Users.id eq id)
                    .map { User.fromRow(it) }
                    .singleOrNull()
            }

            if (user != null) {
                call.respond(user)
            } else {
                call.respondWithError(HttpStatusCode.NotFound, "User not found")
            }
        }

        put("/{id}") {
            observability.meter.increment("user.update", "{request}", description = "Update user")
            val id = call.parameters["id"]?.toIntOrNull()
            val updatedUser = call.receive<UpdateUserRequest>()
            val user = Database.runQuery {
                Users.update({ Users.id eq id }) {
                    it[name] = updatedUser.name
                    it[updatedAt] = LocalDateTime.now()
                }
                Users.selectAll()
                    .where(Users.id eq id)
                    .map { User.fromRow(it) }
                    .singleOrNull()
            }

            if (user != null) {
                call.respond(user)
            } else {
                call.respondWithError(HttpStatusCode.NotFound, "User not found")
            }
        }

        delete("/{id}") {
            observability.meter.increment("user.delete", "{request}", description = "Delete user")
            val id = call.parameters["id"]?.toIntOrNull()
            val removed = Database.runQuery {
                Users.deleteWhere { Users.id eq id } > 0
            }

            if (removed) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
    }
}