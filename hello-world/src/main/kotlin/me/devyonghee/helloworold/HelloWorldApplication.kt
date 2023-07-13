package me.devyonghee.batchhelloworold

import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager

@SpringBootApplication
class HelloWorldApplication(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    fun job() {
    }

    @Bean
    fun step(): Step {
        return StepBuilder("step1", jobRepository)
            .tasklet({ _, _ ->
                print("Hello, world")
                RepeatStatus.FINISHED
            }, transactionManager).build()
    }
}

fun main(args: Array<String>) {
    runApplication<HelloWorldApplication>(*args)
}
