package ru.otus.support.db

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux
import ru.otus.support.db.entity.MessageEntity

interface MessageRepository : R2dbcRepository<MessageEntity, Long> {
    @Query("SELECT * FROM message WHERE client_id = :clientId ORDER BY time desc LIMIT :count")
    fun findByClient(clientId: Long, count: Int): Flux<MessageEntity>
}