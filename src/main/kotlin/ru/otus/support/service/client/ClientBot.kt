package ru.otus.support.service.client

import com.github.kotlintelegrambot.entities.BotCommand
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.otus.support.service.MessageService
import ru.otus.support.service.SupportService
import ru.otus.support.service.telegram.createBot

@Service
class ClientBot(
    @Value("\${client-bot.token}")
    token: String,
    service: SupportService,
    messageService: MessageService
) {
    private val bot = createBot(token, messageService,
        { user, chatId -> service.clientText(user, chatId, bot, text) },
        BotCommand("start", "Начать работу")
                to { user, chatId -> service.startClient(user, chatId, bot) },
        )
}