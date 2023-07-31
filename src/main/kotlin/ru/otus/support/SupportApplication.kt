package ru.otus.support

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SupportApplication

fun main(args: Array<String>) {
    runApplication<SupportApplication>(*args)
}
