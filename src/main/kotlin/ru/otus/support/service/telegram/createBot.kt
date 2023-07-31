package ru.otus.support.service.telegram

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.BotCommand
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.logging.LogLevel
import ru.otus.support.service.MessageService

typealias CommandAction = suspend CommandHandlerEnvironment.(user: User, charId: ChatId) -> Unit
typealias TextAction = suspend TextHandlerEnvironment.(user: User, charId: ChatId) -> Unit

private class UserAndChat(val user: User?, val chatId: ChatId, val bot: Bot, val messageService: MessageService) {
    suspend operator fun invoke(block: suspend (user: User, chatId: ChatId) -> Unit) {
        if (user == null) {
            bot.sendMessage(chatId, messageService.cantWork)
        } else {
            block(user, chatId)
        }
    }
}

fun createBot(
    token: String,
    messageService: MessageService,
    textAction: TextAction,
    vararg commandAndAction: Pair<BotCommand, CommandAction>,
): Bot = bot {
    this.token = token
    logLevel = LogLevel.Error

    dispatch {
        commandAndAction.forEach { (cmd, action) ->
            command(cmd.command) {
                UserAndChat(message.from,  ChatId.fromId(message.chat.id), bot, messageService)() { user, chatId ->
                    action(user, chatId)
                }
            }
        }

        text {
            UserAndChat(message.from,  ChatId.fromId(message.chat.id), bot, messageService)() { user, chatId ->
                textAction(user, chatId)
            }
        }
    }
}.apply {
    setMyCommands(commandAndAction.map { it.first })
    startPolling()
}