package me.devyonghee.customeritemwriter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerItemWriterApplication

fun main(args: Array<String>) {
    //val jobArgs: Array<String> = arrayOf(
    //    "--job.name=formatJob",
    //    "inputFile=/data/customer.csv",
    //    "outputFile=file:customer-item-writer/output/formattedCustomers.txt"
    //)

    //val jobArgs: Array<String> = arrayOf(
    //    "--job.name=xmlFormatJob",
    //    "inputFile=/data/customer.csv",
    //    "outputFile=file:customer-item-writer/output/formattedCustomers.xml"
    //)

    //val jobArgs: Array<String> = arrayOf("--job.name=jdbcFormatJob", "inputFile=/data/customer.csv")
    //val jobArgs: Array<String> = arrayOf("--job.name=jpaFormatJob", "inputFile=/data/customer.csv")
    //val jobArgs: Array<String> = arrayOf("--job.name=mongoFormatJob", "inputFile=/data/customer.csv")
    //val jobArgs: Array<String> = arrayOf("--job.name=itemWriterAdapterFormatJob", "inputFile=/data/customer.csv")
    //val jobArgs: Array<String> = arrayOf("--job.name=emailCustomerJob", "inputFile=/data/customer.csv")
    //val jobArgs: Array<String> = arrayOf("--job.name=multiXmlGeneratorJob", "inputFile=/data/customer.csv")

    //val jobArgs: Array<String> = arrayOf(
    //    "--job.name=compositeCustomerJob",
    //    "inputFile=/data/customer.csv",
    //    "outputFile=file:customer-item-writer/output/formattedCustomers.xml"
    //)

    val jobArgs: Array<String> = arrayOf(
        "--job.name=classifierCustomerJob",
        "inputFile=/data/customer.csv",
        "outputFile=file:customer-item-writer/output/formattedCustomers.xml"
    )

    runApplication<CustomerItemWriterApplication>(*args, *jobArgs)
}