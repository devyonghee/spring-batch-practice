package me.devyonghee.optimizationbatch

import java.util.concurrent.Future
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.integration.async.AsyncItemProcessor
import org.springframework.batch.integration.async.AsyncItemWriter
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class AsyncJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val multiThreadedWriter: ItemWriter<Transaction>,
    private val transactionItemReader: FlatFileItemReader<Transaction>,
) {
    @Bean
    fun asyncJob(): Job {
        return JobBuilder("asyncJob", jobRepository)
            .start(step1Async())
            .build()
    }

    @Bean
    fun step1Async(): Step {
        return StepBuilder("step1Async", jobRepository)
            .chunk<Transaction, Future<Transaction>>(100, transactionManager)
            .reader(transactionItemReader)
            .processor(asyncItemProcessor())
            .writer(asyncWriter())
            .build()
    }

    @Bean
    fun asyncItemProcessor(): AsyncItemProcessor<Transaction, Transaction> {
        return AsyncItemProcessor<Transaction, Transaction>().apply {
            setDelegate(sleepProcessor())
            setTaskExecutor(SimpleAsyncTaskExecutor())
        }
    }

    @Bean
    fun sleepProcessor(): ItemProcessor<Transaction, Transaction> {
        return ItemProcessor<Transaction, Transaction> {
            Thread.sleep(1000)
            it
        }
    }

    @Bean
    fun asyncWriter(): AsyncItemWriter<Transaction> {
        return AsyncItemWriter<Transaction>().apply {
            setDelegate(multiThreadedWriter)
        }
    }
}