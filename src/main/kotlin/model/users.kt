package com.example.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime

object Users : IntIdTable("users") {
    val name = varchar("name", 255)
    val email = varchar("email", 255).uniqueIndex()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

@Serializable
data class User(val id: Int, val name: String, val email: String, val createdAt: String, val updatedAt: String) {
    companion object {
        fun fromRow(it: ResultRow) =
            User(
                id = it[Users.id].value,
                name = it[Users.name].toString(),
                email = it[Users.email].toString(),
                createdAt = it[Users.createdAt].toString(),
                updatedAt = it[Users.updatedAt].toString()
            )
    }
}

@Serializable
data class CreateUserRequest(val name: String, val email: String)

@Serializable
data class UpdateUserRequest(val name: String)