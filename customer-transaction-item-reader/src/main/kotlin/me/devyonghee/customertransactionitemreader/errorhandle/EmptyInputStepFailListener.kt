package me.devyonghee.customertransactionitemreader.errorhandle

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterStep


object EmptyInputStepFailListener {

    @AfterStep
    fun afterStep(stepExecution: StepExecution): ExitStatus {
        if (stepExecution.readCount > 0) {
            return stepExecution.exitStatus
        }
        return ExitStatus.FAILED
    }
}