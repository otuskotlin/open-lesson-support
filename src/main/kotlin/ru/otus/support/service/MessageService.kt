package ru.otus.support.service

import com.github.kotlintelegrambot.entities.User
import org.springframework.stereotype.Service

@Service
class MessageService {
    val operatorClientWait = "Клиент ожидает ответа, отправьте любое сообщение"
    val operatorClientTimeout = "Вы не подтвердили готовность, клиент передан другому оператору"
    val operatorNoClient = "Ожидайте подключения клиента"
    fun operatorAccept(messageCount: Int) = "Принято. Последние $messageCount сообщений клиента"
    val clientAccepted  = "Оператор подключился к решению вашего вопроса"
    val operatorInactivity = "Вы не ответили клиенту, он передан другому оператору"
    val clientInactivity = "Спасибо за обращение, если у вас остались вопросы, напишите нам"
    val clientWaitOperator = "Идет поиск свободного оператора"
    val operatorClientInactivity = "Превышен таймаут ожидания клиента, клеинт отключен"
    fun operatorHello(user: User) = """
                    Привет, ${user.username}!
                    Вы начали работать оператором
                    При появлении клиента вы будете уведомлены и должны за 5 секунд подтвердить, отправив любое сообщение.
                    После этого вы получите несколько предыдущих сообщений (при наличии) и текущий вопрос
                    """.trimIndent()
    val operatorMeedStart = "Отправьте команду /start"
    val cantWork = "Я не могу с вами работать"
    val noFreeOperators = "Нет свободных операторов, напишите пожалуйста позже"
}