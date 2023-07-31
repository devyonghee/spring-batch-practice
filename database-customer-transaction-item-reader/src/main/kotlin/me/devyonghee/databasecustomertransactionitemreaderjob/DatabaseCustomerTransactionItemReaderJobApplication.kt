package me.devyonghee.databasecustomertransactionitemreaderjob

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatabaseCustomerTransactionItemReaderJobApplication

fun main(args: Array<String>) {
    val jobArgs = arrayOf("--job.name=jdbcJob", "city=Chicago")

    runApplication<DatabaseCustomerTransactionItemReaderJobApplication>(*args, *jobArgs)
}