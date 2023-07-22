package me.devyonghee.jobexplorer

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobInstance
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

class ExploringTasklet(
    private val explorer: JobExplorer,
) : Tasklet {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus? {
        val jobName: String = chunkContext.stepContext.jobName
        val instances: List<JobInstance> = explorer.getJobInstances(jobName, 0, Int.MAX_VALUE)
        log.info("There are {} job instances for the job {}", instances.size, jobName)

        instances.forEach {
            val jobExecutions: List<JobExecution> = explorer.getJobExecutions(it)

            log.info("instance {} had {} executions", it.instanceId, jobExecutions.size)
            jobExecutions.forEach { execution ->
                log.info("execution {} resulted in Exit Status {}", execution.id, execution.exitStatus)
            }
        }

        return RepeatStatus.FINISHED
    }
}