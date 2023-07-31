package ru.otus.support.service.common

import com.github.kotlintelegrambot.entities.ChatId

data class UserChatAndBot(val user: UserId, val chatId: ChatId, var botId: UserId?)