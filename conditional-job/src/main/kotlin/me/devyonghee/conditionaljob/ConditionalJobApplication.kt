package me.devyonghee.conditionaljob

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ConditionalJobApplication

fun main(args: Array<String>) {
    runApplication<ConditionalJobApplication>(*args)
}