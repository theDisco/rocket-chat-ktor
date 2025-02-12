package com.example

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.javatime.datetime

object Conversations : IntIdTable("conversations") {
    val title = varchar("title", 255)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object MessageStatuses : IntIdTable("message_statuses") {
    val userId = integer("user_id").index().references(Users.id)
    val messageId = integer("message_id").index().references(Messages.id)
    val status = integer("status").default(0)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Messages : IntIdTable("messages") {
    val content = text("content")
    val userId = integer("user_id").index().references(Users.id)
    val conversationId = integer("conversation_id").index().references(Conversations.id)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Participants : IntIdTable("participants") {
    val userId = integer("user_id").index().references(Users.id)
    val conversationId = integer("conversation_id").index().references(Conversations.id)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

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
data class UserRequest(val name: String, val email: String)
