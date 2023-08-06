package me.devyonghee.customeritemreader.custom

import me.devyonghee.customeritemreader.jdbc.Customer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class CustomCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun customJob(): Job {
        return JobBuilder("customJob", jobRepository)
            .start(customStep())
            .build()
    }

    @Bean
    fun customStep(): Step {
        return StepBuilder("customStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(customItemReader())
            .writer(itemWriter())
            .build()
    }

    @Bean
    fun customItemReader(): CustomerItemReader {
        return CustomerItemReader().apply {
            name = "customItemReader"
        }
    }

    fun itemWriter(): ItemWriter<Customer> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }
}