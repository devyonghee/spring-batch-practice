package me.devyonghee.customeritemprocessor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.validator.BeanValidatingItemProcessor
import org.springframework.batch.item.validator.ValidatingItemProcessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ValidationCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun validationJob(): Job {
        return JobBuilder("validationJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(validationCopyFileStep())
            .build()
    }

    @Bean
    fun validationCopyFileStep(): Step {
        return StepBuilder("validationCopyFileStep", jobRepository)
            .chunk<Customer, Customer>(3, transactionManager)
            .reader(validationCustomerItemReader(PathResource("")))
            .processor(customerValidatingItemProcessor())
            .writer(itemWriter())
            .build()
    }

    fun itemWriter(): ItemWriter<Customer> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }

    @Bean
    fun customerValidatingItemProcessor(): ValidatingItemProcessor<Customer> {
        return ValidatingItemProcessor<Customer>(validator())
    }

    @Bean
    fun validator(): UniqueLastNameValidator {
        return UniqueLastNameValidator().apply {
            name = "validator"
        }
    }

    @Bean
    fun beanValidatingItemProcessor(): BeanValidatingItemProcessor<Customer> {
        // jakarta.validation.Validator 로 검증하는 경우
        return BeanValidatingItemProcessor<Customer>()
    }

    @Bean
    @StepScope
    fun validationCustomerItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource,
    ): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("validationCustomerItemReader")
            .resource(inputFile)
            .delimited()
            .names(
                "firstName",
                "middleInitial",
                "lastName",
                "address",
                "city",
                "state",
                "zip",
            ).fieldSetMapper {
                Customer(
                    it.readString("firstName"),
                    it.readString("middleInitial"),
                    it.readString("lastName"),
                    it.readString("address"),
                    it.readString("city"),
                    it.readString("state"),
                    it.readString("zip")
                )
            }.build()
    }
}