package me.devyonghee.simplejobstep

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeStep

object AnnotationLoggingStepListener {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @BeforeStep
    fun beforeStep(stepExecution: StepExecution) {
        log.info("{} step is beginning execution", stepExecution.stepName)
    }

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        log.info("{} step has completed", stepExecution.stepName)
        return stepExecution.exitStatus
    }
}