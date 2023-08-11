package me.devyonghee.customizejobrepository

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomizeJobRepositoryApplication

fun main(args: Array<String>) {
    val jobArgs = arrayOf("--job.name=validationJob")

    runApplication<CustomizeJobRepositoryApplication>(*args)
}