package me.devyonghee.databasecustomertransactionitemreaderjob

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatabaseCustomerTransactionItemReaderJobApplication

fun main(args: Array<String>) {
    val jobArgs = arrayOf("--job.name=jsonFileJob", "customerFile=input/customer.json")

    runApplication<DatabaseCustomerTransactionItemReaderJobApplication>(*args, *jobArgs)
}