package ru.otus.support.service

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

//@Service
class HelloBot(
    @Value("\${client-bot.token}")
    token: String,
) {
    private val bot = bot {
        this.token = token
        dispatch {
            text {
                bot.sendMessage(ChatId.fromId(message.chat.id), text = "Hello, $text")
            }
        }
    }.apply { startPolling() }
}