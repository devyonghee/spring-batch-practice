package me.devyonghee.databasecustomertransactionitemreader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatabaseCustomerTransactionItemReaderJobApplication

fun main(args: Array<String>) {
    // val jobArgs = arrayOf("--job.name=jdbcJob", "city=Chicago")
    val jobArgs = arrayOf("--job.name=jpaJob", "city=Chicago")

    runApplication<DatabaseCustomerTransactionItemReaderJobApplication>(*args, *jobArgs)
}