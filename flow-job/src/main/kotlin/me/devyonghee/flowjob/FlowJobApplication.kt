package me.devyonghee.flowjob

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.job.DefaultJobParametersExtractor
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.transaction.PlatformTransactionManager

@SpringBootApplication
class FlowJobApplication(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun conditionalStepLogicJob(): Job {
        return JobBuilder("conditionalStepLogicJob", jobRepository)
            // 독자적인 플로우 생성
            //.start(preProcessingFlow())
            // 플로우 스텝 사용
            //.start(initializeBatch())
            // 잡 스텝 사용
            .start(initializeBatchJobStep())
            .next(runBatch())
            //.end()
            .build()
    }

    @Bean
    fun initializeBatch(): Step {
        return StepBuilder("initializeBatch", jobRepository)
            .flow(preProcessingFlow())
            .build()
    }

    @Bean
    fun preProcessingFlow(): Flow {
        return FlowBuilder<Flow>("preProcessingFlow")
            .start(loadFileStep())
            .next(loadCustomerStep())
            .next(updateStartStep())
            .build()
    }


    @Bean
    // 잡 스텝 사용
    fun initializeBatchJobStep(): Step {
        return StepBuilder("initializeBatchJobStep", jobRepository)
            .job(preProcessingJob())
            .parametersExtractor(DefaultJobParametersExtractor())
            .build()
    }

    @Bean
    fun preProcessingJob(): Job {
        return JobBuilder("preProcessingJob", jobRepository)
            .start(loadFileStep())
            .next(loadCustomerStep())
            .next(updateStartStep())
            .build()
    }

    @Bean
    fun loadFileStep(): Step {
        return StepBuilder("loadFileStep", jobRepository)
            .tasklet(loadStockFile(), transactionManager)
            .build()
    }

    @Bean
    fun loadStockFile(): Tasklet {
        return Tasklet { _, _ ->
            log.info("the stock file has been loaded")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun loadCustomerStep(): Step {
        return StepBuilder("loadCustomerStep", jobRepository)
            .tasklet(loadCustomerFile(), transactionManager)
            .build()
    }

    @Bean
    fun loadCustomerFile(): Tasklet {
        return Tasklet { _, _ ->
            log.info("the customer file has been loaded")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun updateStartStep(): Step {
        return StepBuilder("updateStartStep", jobRepository)
            .tasklet(updateStart(), transactionManager)
            .build()
    }

    @Bean
    fun updateStart(): Tasklet {
        return Tasklet { _, _ ->
            log.info("the start has been updated")
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun runBatch(): Step {
        return StepBuilder("runBatch", jobRepository)
            .tasklet(runBatchTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun runBatchTasklet(): Tasklet {
        return Tasklet { _, _ ->
            log.info("the batch has been run")
            RepeatStatus.FINISHED
        }
    }
}

fun main(args: Array<String>) {
    runApplication<FlowJobApplication>(*args)
}