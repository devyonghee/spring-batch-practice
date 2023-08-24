package me.devyonghee.apressbanking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApressBankingApplication


fun main(args: Array<String>) {
    val jobArgs: Array<String> = arrayOf(
        "customerUpdateFile=/input/customer_update.csv",
        "transactionFile=/input/transactions.json",
        "outputDirectory=file:apress-banking-expample/output/"
    )

    runApplication<ApressBankingApplication>(*jobArgs, *args)
}