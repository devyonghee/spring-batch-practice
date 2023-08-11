package me.devyonghee.customeritemwriter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerItemWriterApplication

fun main(args: Array<String>) {
    val jobArgs: Array<String> = arrayOf(
        "--job.name=formatJob",
        "inputFile=/data/customer.csv",
        "outputFile=file:customer-item-writer/output/formattedCustomers.txt"
    )

    runApplication<CustomerItemWriterApplication>(*args, *jobArgs)
}