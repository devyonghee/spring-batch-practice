package me.devyonghee.customertransactionitemreader

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CustomerTransactionItemReaderApplication

fun main(args: Array<String>) {
    // val jobArgs = arrayOf("--job.name=flatFileJob", "customerFile=input/customerFile*")
    // val jobArgs = arrayOf("--job.name=xmlFileJob", "customerFile=input/customer.xml")
    // val jobArgs = arrayOf("--job.name=jsonFileJob", "customerFile=input/customer.json")
    // val jobArgs = arrayOf("--job.name=jdbcJob", "city=Chicago")
    // val jobArgs = arrayOf("--job.name=jpaJob", "city=Chicago")
    // val jobArgs = arrayOf("--job.name=storedProcedureJob", "city=Chicago")
    // val jobArgs = arrayOf("--job.name=mongodbJob", "hashTag=nodejs")
    // val jobArgs = arrayOf("--job.name=serviceJob")
    // val jobArgs = arrayOf("--job.name=customJob")
    //val jobArgs = arrayOf("--job.name=errorHandleJob", "customerFile=input/invalid_customerFile.txt")
    val jobArgs = arrayOf("--job.name=errorHandleJob", "customerFile=input/empty_customerFile.txt")

    runApplication<CustomerTransactionItemReaderApplication>(*args, *jobArgs)
}