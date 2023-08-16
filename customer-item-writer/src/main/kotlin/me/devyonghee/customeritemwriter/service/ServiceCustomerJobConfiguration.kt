package me.devyonghee.customeritemwriter.service

import me.devyonghee.customeritemwriter.flatfile.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.adapter.ItemWriterAdapter
import org.springframework.batch.item.adapter.PropertyExtractingDelegatingItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ServiceCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val itemReader: FlatFileItemReader<Customer>,
    private val customerService: CustomerService,
) {
    @Bean
    fun itemWriterAdapterFormatJob(): Job {
        return JobBuilder("itemWriterAdapterFormatJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(serviceFormatStep())
            .build()
    }

    @Bean
    fun serviceFormatStep(): Step {
        return StepBuilder("serviceFormatStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(itemReader)
            .writer(customerWriterAdapter())
            .writer(customerAddressItemWriter())
            .build()
    }

    @Bean
    fun customerWriterAdapter(): ItemWriterAdapter<Customer> {
        return ItemWriterAdapter<Customer>().apply {
            setTargetObject(customerService)
            setTargetMethod("logCustomer")
        }
    }

    @Bean
    fun customerAddressItemWriter(): PropertyExtractingDelegatingItemWriter<Customer> {
        return PropertyExtractingDelegatingItemWriter<Customer>().apply {
            setTargetObject(customerService)
            setTargetMethod("logCustomerAddress")
            setFieldsUsedAsTargetMethodArguments(arrayOf("address", "city", "state", "zipCode"))
        }
    }
}