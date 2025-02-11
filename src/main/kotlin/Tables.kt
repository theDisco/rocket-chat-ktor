package com.example

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.javatime.datetime

object Conversations : IntIdTable("conversations") {
    val title = varchar("title", 255).isNotNull()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object MessageStatuses : IntIdTable("message_statuses") {
    val userId = integer("user_id").index().references(Users.id).isNotNull()
    val messageId = integer("message_id").index().references(Messages.id).isNotNull()
    val status = integer("status").default(0).isNotNull()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Messages : IntIdTable("messages") {
    val content = text("content").isNotNull()
    val userId = integer("user_id").index().references(Users.id).isNotNull()
    val conversationId = integer("conversation_id").index().references(Conversations.id).isNotNull()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Participants : IntIdTable("participants") {
    val userId = integer("user_id").index().references(Users.id).isNotNull()
    val conversationId = integer("conversation_id").index().references(Conversations.id).isNotNull()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

object Users : IntIdTable("users") {
    val name = varchar("name", 255).isNotNull()
    val email = varchar("email", 255).uniqueIndex().isNotNull()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}
