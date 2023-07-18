package me.devyonghee.conditionaljob

import kotlin.random.Random
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.job.flow.FlowExecutionStatus
import org.springframework.batch.core.job.flow.JobExecutionDecider

object RandomDecider : JobExecutionDecider {

    private val random: Random = Random.Default

    override fun decide(jobExecution: JobExecution, stepExecution: StepExecution?): FlowExecutionStatus {
        return if (random.nextBoolean()) {
            FlowExecutionStatus(FlowExecutionStatus.COMPLETED.name)
        } else {
            FlowExecutionStatus(FlowExecutionStatus.FAILED.name)
        }
    }
}