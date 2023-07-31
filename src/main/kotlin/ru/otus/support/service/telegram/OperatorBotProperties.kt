package ru.otus.support.service.telegram

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

@ConfigurationProperties("operator-bot")
data class OperatorBotProperties(
    val tokens: List<String>,
) {
}