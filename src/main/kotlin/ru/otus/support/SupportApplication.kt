package ru.otus.support

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SupportApplication

fun main(args: Array<String>) {
    val ctx = runApplication<SupportApplication>(*args)
    println(
        """    
        count = ${ctx.beanDefinitionCount}
        """.trimIndent()
    )
}
