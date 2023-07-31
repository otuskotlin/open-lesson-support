package ru.otus.support.db.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table(name = "message")
data class MessageEntity(
    @Id
    val messageId: String = UUID.randomUUID().toString(),
    val clientId: Long,
    val time: Instant = Instant.now(),
    val message: String,
    val isClient: Boolean,
    @Transient
    var new: Boolean
) : Persistable<String> {

    @PersistenceCreator
    constructor(messageId: String, clientId: Long, time: Instant, message: String, isClient: Boolean) :
            this(messageId, clientId, time, message, isClient, false)

    override fun getId(): String = messageId

    override fun isNew(): Boolean = new
}