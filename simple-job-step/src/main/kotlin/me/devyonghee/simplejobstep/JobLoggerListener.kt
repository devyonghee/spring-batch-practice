package me.devyonghee.simplejobstep

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobExecutionListener

object JobLoggerListener : JobExecutionListener {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun beforeJob(jobExecution: JobExecution) {
        log.info("{} is beginning execution", jobExecution.jobInstance.jobName)
    }

    override fun afterJob(jobExecution: JobExecution) {
        log.info("{} has completed with the status {}", jobExecution.jobInstance.jobName, jobExecution.status)
    }
}