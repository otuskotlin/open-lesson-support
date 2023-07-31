package ru.otus.support.service.operator

import com.github.kotlintelegrambot.entities.BotCommand
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Service
import ru.otus.support.service.MessageService
import ru.otus.support.service.SupportService
import ru.otus.support.service.telegram.createBot
import ru.otus.support.service.telegram.OperatorBotProperties

@Service
@EnableConfigurationProperties(OperatorBotProperties::class)
class OperatorBot(
    properties: OperatorBotProperties,
    service: SupportService,
    messageService: MessageService
) {
    private val bot = properties.tokens.map { token ->
        createBot(
            token, messageService,
            { user, chatId -> service.operatorText(user, chatId, bot, text) },
            BotCommand("start", "Начать работу")
                    to { user, chatId -> service.startOperator(user, chatId, bot) },
            BotCommand("finish", "Завершить работу")
                    to { user, chatId -> service.removeOperator(user, chatId, bot) },
        )
    }
}
