package ru.otus.support.service.operator

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

@ConfigurationProperties("operator")
data class OperatorProperties(
    val acceptTimeout: Duration,
    val inactivityTimeout: Duration,
    val historySize: Int = 5
) {
}