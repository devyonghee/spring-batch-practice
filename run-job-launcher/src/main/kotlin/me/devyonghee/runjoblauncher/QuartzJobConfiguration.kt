package me.devyonghee.runjoblauncher

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class QuartzJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun quartzJob(): Job {
        return JobBuilder("queartzJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(quartzStep1())
            .build()
    }

    @Bean
    fun quartzStep1(): Step {
        return StepBuilder("quartzStep1", jobRepository)
            .tasklet({ _, _ ->
                log.info("quartz step1 ran!")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }
}