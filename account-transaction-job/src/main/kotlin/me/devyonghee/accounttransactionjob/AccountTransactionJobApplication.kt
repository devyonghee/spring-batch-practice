package me.devyonghee.accounttransactionjob

import java.sql.ResultSet
import javax.sql.DataSource
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import org.springframework.transaction.PlatformTransactionManager

@SpringBootApplication
class AccountTransactionJobApplication(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    // 중지 트랜지션을 사용해 중지한 경우
    // @Bean
    // fun transactionJob(): Job {
    //     return JobBuilder("transactionJob", jobRepository)
    //         .start(importTransactionFileStep())
    //         .on(ExitStatus.STOPPED.exitCode).stopAndRestart(importTransactionFileStep())
    //         .from(importTransactionFileStep()).on("*").to(applyTransactionStep())
    //         .from(applyTransactionStep()).next(generateSummaryStep())
    //         .end()
    //         .build()
    // }

    // StepExecution 을 사용해 중지한 경우
    @Bean
    fun transactionJob(): Job {
        return JobBuilder("transactionJob", jobRepository)
            .preventRestart() // 잡이 실패하거나 중지된 경우 다시 실행하지 않음
            .start(importTransactionFileStep())
            .next(applyTransactionStep())
            .next(generateSummaryStep())
            .build()
    }

    @Bean
    fun importTransactionFileStep(): Step {
        return StepBuilder("importTransactionFileStep", jobRepository)
            // 재시작 횟수 제한
            .startLimit(2)
            // 완료된 스텝 재실행 설정
            .allowStartIfComplete(true)
            .chunk<Transaction, Transaction>(100, transactionManager)
            .reader(transactionReader())
            .writer(transactionWriter())
            .allowStartIfComplete(true)
            .listener(transactionReader())
            .build()
    }

    @Bean
    fun transactionReader(): ItemReader<Transaction> {
        return TransactionReader(fileItemReader(PathResource("")))
    }

    @Bean
    @StepScope
    fun fileItemReader(
        @Value("#{jobParameters['transactionFile']}") inputFile: Resource,
    ): FlatFileItemReader<FieldSet> {
        return FlatFileItemReaderBuilder<FieldSet>()
            .name("fileItemReader")
            .resource(inputFile)
            .lineTokenizer(DelimitedLineTokenizer())
            .fieldSetMapper(PassThroughFieldSetMapper())
            .build()
    }

    @Bean
    fun transactionWriter(): JdbcBatchItemWriter<Transaction> {
        return JdbcBatchItemWriterBuilder<Transaction>()
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .sql(
                """
                INSERT INTO TRANACTION(ACCOUNT_SUMMARY_ID, TIMESTAMP, AMOUNT) 
                VALUES ((SELECT ID FROM ACCOUNT_SUMMARY WHERE ACCOUNT_NUMBER = :accountNumber), :timestamp, :amount)
            """.trimIndent()
            ).dataSource(dataSource)
            .build()
    }

    @Bean
    fun applyTransactionStep(): Step {
        return StepBuilder("applyTransactionStep", jobRepository)
            .chunk<AccountSummary, AccountSummary>(100, transactionManager)
            .reader(accountSummaryReader(dataSource))
            .processor(transactionApplicatorProcessor())
            .writer(accountSummaryWriter())
            .allowStartIfComplete(true)
            .build()
    }

    @Bean
    @StepScope
    fun accountSummaryReader(dataSource: DataSource): JdbcCursorItemReader<AccountSummary> {
        return JdbcCursorItemReaderBuilder<AccountSummary>()
            .name("accountSummaryReader")
            .dataSource(dataSource)
            .sql(
                """
                SELECT ID, ACCOUNT_NUMBER, CURRENT_BALANCE 
                    FROM ACCOUNT_SUMMARY A
                WHERE A.ID IN (
                    SELECT DISTINCT T.ACCOUNT_SUMMARY_ID
                    FROM TRANACTION T
                )
                ORDER BY A.ACCOUNT_NUMBER
            """.trimIndent()
            ).rowMapper { resultSet: ResultSet, _: Int ->
                AccountSummary(
                    id = resultSet.getInt("id"),
                    accountNumber = resultSet.getString("account_number"),
                    currentBalance = resultSet.getDouble("current_balance"),
                )
            }.build()
    }

    @Bean
    fun transactionDao(): TransactionDaoSupport {
        return TransactionDaoSupport(dataSource)
    }

    @Bean
    fun transactionApplicatorProcessor(): TransactionApplierProcessor {
        return TransactionApplierProcessor(transactionDao())
    }

    @Bean
    fun accountSummaryWriter(): JdbcBatchItemWriter<AccountSummary> {
        return JdbcBatchItemWriterBuilder<AccountSummary>()
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .sql(
                """
                UPDATE ACCOUNT_SUMMARY SET CURRENT_BALANCE = :currentBalance 
                    WHERE ACCOUNT_NUMBER = :accountNumber
            """.trimIndent()
            ).dataSource(dataSource)
            .build()
    }

    @Bean
    fun generateSummaryStep(): Step {
        return StepBuilder("generateSummaryStep", jobRepository)
            .chunk<AccountSummary, AccountSummary>(100, transactionManager)
            .reader(accountSummaryReader(dataSource))
            .writer(accountSummaryFileWriter(PathResource("")))
            .build()
    }

    @Bean
    @StepScope
    fun accountSummaryFileWriter(
        @Value("#{jobParameters['summaryFile']}") summaryFile: WritableResource,
    ): FlatFileItemWriter<AccountSummary> {
        val fieldExtractor: BeanWrapperFieldExtractor<AccountSummary> =
            BeanWrapperFieldExtractor<AccountSummary>().apply {
                setNames(arrayOf("accountNumber", "currentBalance"))
                afterPropertiesSet()
            }
        val lineAggregator: DelimitedLineAggregator<AccountSummary> = DelimitedLineAggregator<AccountSummary>().apply {
            setFieldExtractor(fieldExtractor)
        }

        return FlatFileItemWriterBuilder<AccountSummary>()
            .name("accountSummaryFileWriter")
            .resource(summaryFile)
            .lineAggregator(lineAggregator)
            .build()
    }
}

fun main() {
    runApplication<AccountTransactionJobApplication>()
}