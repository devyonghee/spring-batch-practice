package me.devyonghee.customertransactionitemreaderjob

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerTransactionItemReaderJobApplication

fun main(args: Array<String>) {
    val jobArgs = arrayOf("--job.name=flatFileJob", "customerFile=input/customer*")

    runApplication<CustomerTransactionItemReaderJobApplication>(*args, *jobArgs)
}