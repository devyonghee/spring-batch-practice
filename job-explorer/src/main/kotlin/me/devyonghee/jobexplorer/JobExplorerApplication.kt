package me.devyonghee.jobexplorer

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager

@SpringBootApplication
class JobExplorerApplication(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jobExplorer: JobExplorer,
) {
    @Bean
    fun explorerTasklet(): Tasklet {
        return ExploringTasklet(jobExplorer)
    }

    @Bean
    fun explorerStep(): Step {
        return StepBuilder("explorerStep", jobRepository)
            .tasklet(explorerTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun explorerJob(): Job {
        return JobBuilder("explorerJob", jobRepository)
            .start(explorerStep())
            .build()
    }
}

fun main(args: Array<String>) {
    runApplication<JobExplorerApplication>(*args)
}