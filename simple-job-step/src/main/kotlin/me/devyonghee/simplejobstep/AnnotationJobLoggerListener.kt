package me.devyonghee.simplejobstep

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.annotation.AfterJob
import org.springframework.batch.core.annotation.BeforeJob

object AnnotationJobLoggerListener {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @BeforeJob
    fun beforeJob(jobExecution: JobExecution) {
        log.info("{} is beginning execution", jobExecution.jobInstance.jobName)
    }

    @AfterJob
    fun afterJob(jobExecution: JobExecution) {
        log.info("{} has completed with the status {}", jobExecution.jobInstance.jobName, jobExecution.status)
    }
}