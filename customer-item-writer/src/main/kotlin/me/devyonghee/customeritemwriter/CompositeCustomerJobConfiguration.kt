package me.devyonghee.customeritemwriter

import me.devyonghee.customeritemwriter.flatfile.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.support.CompositeItemWriter
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class CompositeCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val customerFileReader: FlatFileItemReader<Customer>,
    private val jdbcCustomerWriter: JdbcBatchItemWriter<Customer>,
    private val xmlCustomerWriter: StaxEventItemWriter<Customer>,
) {
    @Bean
    fun compositeCustomerJob(): Job {
        return JobBuilder("compositeCustomerJob", jobRepository)
            .start(compositeCustomerStep())
            .build()
    }

    @Bean
    fun compositeCustomerStep(): Step {
        return StepBuilder("compositeCustomerStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(customerFileReader)
            .writer(compositeWriter())
            .build()
    }

    @Bean
    fun compositeWriter(): CompositeItemWriter<Customer> {
        return CompositeItemWriterBuilder<Customer>()
            .delegates(listOf(jdbcCustomerWriter, xmlCustomerWriter))
            .build()
    }
}