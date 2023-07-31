package ru.otus.support.service.client

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.User
import ru.otus.support.service.common.UserChatAndBot
import ru.otus.support.service.common.UserId
import ru.otus.support.service.operator.OperatorSession

class ClientSession(
    val user: User,
    val chatId: ChatId,
    val bot: Bot,
) {
    var operatorSession: OperatorSession? = null

    fun sendMessage(text: String) {
        bot.sendMessage(chatId, text)
    }

    val key get() = UserChatAndBot(UserId(user.id), chatId, null)
}