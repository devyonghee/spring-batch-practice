package me.devyonghee.customeritemprocessor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerItemProcessorApplication

fun main(args: Array<String>) {
    val jobArgs = arrayOf("--job.name=validationJob", "customerFile=/input/customer.csv")

    runApplication<CustomerItemProcessorApplication>(*args, *jobArgs)
}