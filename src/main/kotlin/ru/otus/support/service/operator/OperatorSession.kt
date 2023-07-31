package ru.otus.support.service.operator

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import ru.otus.support.db.entity.MessageEntity
import ru.otus.support.service.SupportService
import ru.otus.support.service.common.UserChatAndBot
import ru.otus.support.service.common.UserId
import ru.otus.support.service.client.ClientSession
import java.util.concurrent.atomic.AtomicInteger

class OperatorSession(
    private val user: User,
    chatId: ChatId,
    private val service: SupportService,
    private val bot: Bot,
) {
    val key = UserChatAndBot(UserId(user.id), chatId, UserId(bot.getMe().get().id))
    private val mutex = Mutex()
    private val id = nextId.incrementAndGet()
    private val log = LoggerFactory.getLogger("operator.session.$id.${user.username ?: user.id}")
    private var state: State = IdleState()

    fun start() {
        log.info("Started")
        bot.sendMessage(key.chatId, service.messageService.operatorHello(user))
    }

    suspend fun acceptClient(client: ClientSession): Boolean = mutex.withLock {
        state.acceptClient(client)
    }

    suspend fun operatorText(text: String): Unit = mutex.withLock {
        state.operatorText(text)
    }

    suspend fun clientText(text: String): Unit = mutex.withLock {
        state.clientText(text)
    }

    suspend fun destroy() {
        mutex.withLock {
            state.destroy()
            setState(DestroyState())
        }
    }

    fun sendMessageToOperator(text: String) {
        bot.sendMessage(key.chatId, text)
    }

    private suspend fun setState(newState: State) {
        log.info("Change state from ${state::class.simpleName} to ${newState::class.simpleName}")
        state.complete()
        state = newState
        state.start()
    }

    companion object {
        private val nextId = AtomicInteger()
    }

    private interface State {
        suspend fun acceptClient(client: ClientSession): Boolean = false
        suspend fun operatorText(text: String) {}
        suspend fun clientText(text: String) {}
        suspend fun start() {}
        suspend fun destroy() {}
        suspend fun complete() {}
    }

    private inner class IdleState : State {
        override suspend fun acceptClient(client: ClientSession): Boolean {
            setState(AcceptState(client))
            return true
        }

        override suspend fun operatorText(text: String) {
            bot.sendMessage(key.chatId, service.messageService.operatorNoClient)
        }
    }

    private inner class AcceptState(private val client: ClientSession) : State {
        private var cancel = false
        private val notifyJob = GlobalScope.launch {
            sendMessageToOperator(service.messageService.operatorClientWait)

            var wait = service.properties.acceptTimeout.toMillis()
            var interval = 1000L
            while (wait > interval) {
                delay(interval)
                wait -= interval
                interval *= 2
                sendMessageToOperator(service.messageService.operatorClientWait)
            }

            delay(interval)
            mutex.withLock {
                if (isActive) return@launch
                cancel = true

                sendMessageToOperator(service.messageService.operatorClientTimeout)
                setState(IdleState())
            }
            service.acceptClient(client, this@OperatorSession)
        }

        override suspend fun operatorText(text: String) {
            if (cancel) return
            notifyJob.cancel()

            val resent = service.repository.findByClient(clientId = client.user.id, service.properties.historySize)
                .collectList()
                .awaitSingle()

            sendMessageToOperator(service.messageService.operatorAccept(resent.size))
            client.operatorSession = this@OperatorSession
            client.sendMessage(service.messageService.clientAccepted)

            resent.forEach { msg ->
                sendMessageToOperator(msg.message)
            }

            setState(WaitOperatorState(client))
        }

        override suspend fun destroy() {
            notifyJob.cancel()
            service.acceptClient(client, this@OperatorSession)
        }

        override suspend fun complete() {
            notifyJob.cancel()
        }
    }

    private inner class WaitOperatorState(private val client: ClientSession) : State {
        private var waitJob = GlobalScope.launch {
            delay(service.properties.inactivityTimeout.toMillis())
            mutex.withLock {
                if (!isActive) return@launch
                cancel = true

                setState(IdleState())
                sendMessageToOperator(service.messageService.operatorInactivity)
                service.acceptClient(client, this@OperatorSession)
            }
        }
        private var cancel = false

        override suspend fun operatorText(text: String) {
            if (cancel) return
            waitJob.cancel()

            service.repository.save(
                MessageEntity(
                    clientId = client.user.id,
                    message = text,
                    isClient = false,
                    new = true
                )
            ).awaitSingle()

            client.sendMessage(text)
            setState(WaitClientState(client))
        }

        override suspend fun clientText(text: String) {
            if (cancel) return
            sendMessageToOperator(text)
        }

        override suspend fun destroy() {
            waitJob.cancel()
            service.acceptClient(client, this@OperatorSession)
        }

        override suspend fun complete() {
            waitJob.cancel()
        }
    }

    private inner class WaitClientState(private val client: ClientSession) : State {
        private var waitJob = GlobalScope.launch {
            delay(service.properties.inactivityTimeout.toMillis())
            mutex.withLock {
                if (!isActive) return@launch
                cancel = true

                setState(IdleState())
                client.sendMessage(service.messageService.clientInactivity)
                sendMessageToOperator(service.messageService.operatorClientInactivity)
            }
        }
        private var cancel = false

        override suspend fun operatorText(text: String) {
            if (cancel) return

            service.repository.save(
                MessageEntity(
                    clientId = client.user.id,
                    message = text,
                    isClient = false,
                    new = true
                )
            ).awaitSingle()

            client.sendMessage(text)
        }

        override suspend fun clientText(text: String) {
            if (cancel) return
            sendMessageToOperator(text)
            setState(WaitOperatorState(client))
        }

        override suspend fun complete() {
            waitJob.cancel()
        }

        override suspend fun destroy() {
            waitJob.cancel()
            service.acceptClient(client, this@OperatorSession)
        }
    }

    private class DestroyState : State
}

