package me.devyonghee.helloworld

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager

//@EnableBatchProcessing // 스프링 부트 3 이상부터는 필요하지 않음
@SpringBootApplication
class HelloWorldApplication(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun job(): Job {
        return JobBuilder("job", jobRepository)
            .start(step())
            .build()
    }

    @Bean
    fun step(): Step {
        return StepBuilder("step1", jobRepository)
            .tasklet({ _, _ ->
                log.info("Hello, world")
                RepeatStatus.FINISHED
            }, transactionManager).build()
    }
}

fun main(args: Array<String>) {
    runApplication<HelloWorldApplication>(*args)
}
