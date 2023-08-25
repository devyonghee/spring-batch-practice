package me.devyonghee.optimizationbatch

import java.math.BigDecimal
import java.util.Date
import javax.sql.DataSource
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
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
class ParallelStepsJobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun parallelStepsJob(): Job {
        return JobBuilder("parallelStepsJob", jobRepository)
            .start(
                FlowBuilder<Flow>("secondFlow")
                    .start(parallelStep1())
                    .split(SimpleAsyncTaskExecutor())
                    .add(
                        FlowBuilder<Flow>("secondFlow")
                            .start(parallelStep2())
                            .build()
                    ).build()
            )
            .end()
            .build()
    }

    @Bean
    fun parallelStep1(): Step {
        return StepBuilder("parallelStep1", jobRepository)
            .chunk<Transaction, Transaction>(100, transactionManager)
            .reader(parallelTransactionItemReader1(PathResource("")))
            .writer(parallelStepsWriter())
            .build()
    }

    @Bean
    fun parallelStep2(): Step {
        return StepBuilder("parallelStep2", jobRepository)
            .chunk<Transaction, Transaction>(100, transactionManager)
            .reader(parallelTransactionItemReader2(PathResource("")))
            .writer(parallelStepsWriter())
            .build()
    }


    @Bean
    @StepScope
    fun parallelTransactionItemReader1(
        @Value("#{jobParameters['inputFlatFile1']}") inputFile: Resource,
    ): FlatFileItemReader<Transaction> {
        return flatFileItemReader("parallelTransactionItemReader1", inputFile)
    }

    @Bean
    @StepScope
    fun parallelTransactionItemReader2(
        @Value("#{jobParameters['inputFlatFile2']}") inputFile: Resource,
    ): FlatFileItemReader<Transaction> {
        return flatFileItemReader("parallelTransactionItemReader2", inputFile)
    }

    private fun flatFileItemReader(name: String, inputFile: Resource): FlatFileItemReader<Transaction> {
        return FlatFileItemReaderBuilder<Transaction>()
            .name(name)
            .resource(inputFile)
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
    fun parallelStepsWriter(): JdbcBatchItemWriter<Transaction> {
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