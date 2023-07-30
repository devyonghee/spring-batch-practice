package me.devyonghee.filecustomertransactionitemreaderjob

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FileCustomerTransactionItemReaderApplication

fun main(args: Array<String>) {
    // val jobArgs = arrayOf("--job.name=flatFileJob", "customerFile=input/customerFile*")
    // val jobArgs = arrayOf("--job.name=xmlFileJob", "customerFile=input/customer.xml")
    val jobArgs = arrayOf("--job.name=jsonFileJob", "customerFile=input/customer.json")

    runApplication<FileCustomerTransactionItemReaderApplication>(*args, *jobArgs)
}