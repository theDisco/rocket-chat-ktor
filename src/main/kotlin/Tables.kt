package com.example

import com.example.model.Users
import org.jetbrains.exposed.dao.id.IntIdTable
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


