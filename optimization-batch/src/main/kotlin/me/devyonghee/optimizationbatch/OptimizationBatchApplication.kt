package me.devyonghee.optimizationbatch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OptimizationBatchApplication

fun main(args: Array<String>) {
    //val jobArgs: Array<String> = arrayOf(
    //    "--job.name=multiThreadedJob",
    //    "inputFlatFile=/input/bigtransactions.txt",
    //)
    //val jobArgs: Array<String> = arrayOf(
    //    "--job.name=parallelStepsJob",
    //    "inputFlatFile1=/input/bigtransactions.txt",
    //    "inputFlatFile2=/input/bigtransactions2.txt",
    //)
    //val jobArgs: Array<String> = arrayOf(
    //    "--spring.profiles.active=master",
    //    "--job.name=remotePartitioningJob",
    //    "inputFiles=/input/bigtransactions.txt,/input/bigtransactions2.txt",
    //)

    val jobArgs: Array<String> = arrayOf(
        "--spring.profiles.active=master",
        "--job.name=NONE",
    )

    runApplication<OptimizationBatchApplication>(*jobArgs, *args)
}