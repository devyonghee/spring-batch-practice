package me.devyonghee.customertransactionitemreaderjob

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerTransactionItemReaderJobApplication

fun main(args: Array<String>) {
    // val jobArgs = arrayOf("--job.name=flatFileJob", "customerFile=input/customerFile*")
    // val jobArgs = arrayOf("--job.name=xmlFileJob", "customerFile=input/customer.xml")
    val jobArgs = arrayOf("--job.name=jsonFileJob", "customerFile=input/customer.json")

    runApplication<CustomerTransactionItemReaderJobApplication>(*args, *jobArgs)
}