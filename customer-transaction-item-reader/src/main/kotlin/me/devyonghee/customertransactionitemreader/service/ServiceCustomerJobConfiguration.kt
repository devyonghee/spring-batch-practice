package me.devyonghee.customertransactionitemreader.service

import me.devyonghee.customertransactionitemreader.jdbc.Customer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.adapter.ItemReaderAdapter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ServiceCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val customerService: CustomerService,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun serviceJob(): Job {
        return JobBuilder("serviceJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(serviceStep())
            .build()
    }

    @Bean
    fun serviceStep(): Step {
        return StepBuilder("serviceStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(serviceItemReader())
            .writer(itemWriter())
            .build()
    }

    @Bean
    fun serviceItemReader(): ItemReaderAdapter<Customer> {
        return ItemReaderAdapter<Customer>()
            .apply {
                setTargetObject(customerService)
                setTargetMethod("getCustomers")
            }
    }

    fun itemWriter(): ItemWriter<Customer> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }
}