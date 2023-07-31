package ru.otus.support.service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.User
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import ru.otus.support.db.MessageRepository
import ru.otus.support.db.entity.MessageEntity
import ru.otus.support.service.client.ClientSession
import ru.otus.support.service.common.UserChatAndBot
import ru.otus.support.service.common.UserId
import ru.otus.support.service.operator.OperatorProperties
import ru.otus.support.service.operator.OperatorSession
import java.util.concurrent.ConcurrentHashMap

@Service
@EnableConfigurationProperties(OperatorProperties::class)
class SupportService(
    val properties: OperatorProperties,
    val messageService: MessageService,
    val repository: MessageRepository,
) {
    private val operatorSessions = ConcurrentHashMap<UserChatAndBot, OperatorSession>()
    private val clientSessions = ConcurrentHashMap<UserChatAndBot, ClientSession>()


    suspend fun startOperator(user: User, chatId: ChatId, bot: Bot) {
        val key = UserChatAndBot(UserId(user.id), chatId, UserId(bot.getMe().get().id))
        operatorSessions.put(key, OperatorSession(user, chatId, this, bot).apply { start() })
            ?.also { prev -> prev.destroy() }
    }

    suspend fun operatorText(user: User, chatId: ChatId, bot: Bot, text: String) {
        val key = UserChatAndBot(UserId(user.id), chatId, UserId(bot.getMe().get().id))
        val session = operatorSessions[key]
        if (session == null) {
            bot.sendMessage(chatId, messageService.operatorMeedStart)
        } else {
            session.operatorText(text)
        }
    }

    suspend  fun removeOperator(user: User, chatId: ChatId, bot: Bot) {
        val key = UserChatAndBot(UserId(user.id), chatId, UserId(bot.getMe().get().id))
        operatorSessions.remove(key)?.apply {
            destroy()
        }
    }

    suspend fun startClient(user: User, chatId: ChatId, bot: Bot): ClientSession {
        val key = UserChatAndBot(UserId(user.id), chatId, null)
        var accept = false
        val client = clientSessions.compute(key) { _, value ->
            if (value == null) {
                accept = true
                ClientSession(user, chatId, bot)
            } else {
                value
            }
        }!!
        if (accept) {
            client.sendMessage(messageService.clientWaitOperator)
            acceptClient(client)
        }
        return client
    }

    suspend fun clientText(user: User, chatId: ChatId, bot: Bot, text: String) {
        val client = startClient(user, chatId, bot)

        repository.save(MessageEntity(
            clientId = user.id,
            message = text,
            isClient = true,
            new = true
        )).awaitSingle()

        client.operatorSession?.clientText(text)
    }

    suspend fun acceptClient(client: ClientSession, lastOperatorSession : OperatorSession? = null) {
        client.operatorSession = null
        val operatorSessions = operatorSessions.values.toList()

        if (operatorSessions.isNotEmpty()) {
            var pos = if (lastOperatorSession == null) 0 else {
                val index = operatorSessions.indexOf(lastOperatorSession)
                (index + 1) % operatorSessions.size
            }
            var rest = if (lastOperatorSession == null) operatorSessions.size else operatorSessions.size - 1

            while (rest > 0) {
                rest -= 1
                if (operatorSessions[pos].acceptClient(client)) return
                pos = (pos + 1) % operatorSessions.size
            }
        }

        clientSessions.remove(client.key)
        client.sendMessage(messageService.noFreeOperators)
    }
}