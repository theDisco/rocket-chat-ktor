package com.example.model

import com.example.Users
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow

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