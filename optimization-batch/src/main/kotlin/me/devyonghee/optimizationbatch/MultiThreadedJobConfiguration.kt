package me.devyonghee.optimizationbatch

import java.math.BigDecimal
import java.util.Date
import javax.sql.DataSource
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class MultiThreadedJobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun multiThreadedJob(): Job {
        return JobBuilder("multiThreadedJob", jobRepository)
            .start(multiStep1())
            .build()
    }

    @Bean
    fun multiStep1(): Step {
        return StepBuilder("multiStep1", jobRepository)
            .chunk<Transaction, Transaction>(100, transactionManager)
            .reader(transactionItemReader(PathResource("")))
            .writer(multiThreadedWriter())
            .taskExecutor(SimpleAsyncTaskExecutor())
            .build()
    }

    @Bean
    @StepScope
    fun transactionItemReader(
        @Value("#{jobParameters['inputFlatFile']}") inputFile: Resource,
    ): FlatFileItemReader<Transaction> {
        return FlatFileItemReaderBuilder<Transaction>()
            .name("transactionItemReader")
            .resource(inputFile)
            // 다른 스레드 간에 상태를 공유하지 않도록 설정
            .saveState(false)
            .delimited()
            .names("account", "amount", "timestamp")
            .fieldSetMapper {
                Transaction(
                    it.readString("account"),
                    it.readBigDecimal("amount"),
                    it.readDate("timestamp", "yyyy-MM-dd hh:mm:ss")
                )
            }.build()
    }

    @Bean
    fun multiThreadedWriter(): JdbcBatchItemWriter<Transaction> {
        return JdbcBatchItemWriterBuilder<Transaction>()
            .dataSource(dataSource)
            .sql(
                """
                INSERT INTO transaction (
                    account_id,
                    amount,
                    timestamp
                ) VALUES (
                    :account,
                    :amount,
                    :timestamp
                )
            """.trimIndent()
            ).beanMapped()
            .build()
    }


    data class Transaction(
        val account: String,
        val amount: BigDecimal,
        val timestamp: Date,
    )
}
