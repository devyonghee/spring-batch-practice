package me.devyonghee.customeritemwriter.jpa

import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemWriter
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JpaCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val customerRepository: CustomerRepository,
    private val entityManagerFactory: EntityManagerFactory,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun jpaFormatJob(): Job {
        return JobBuilder("jpaFormatJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(jpaFormatStep())
            .build()
    }

    @Bean
    fun jpaFormatStep(): Step {
        return StepBuilder("jpaFormatStep", jobRepository)
            .chunk<CustomerEntity, CustomerEntity>(10, transactionManager)
            .reader(jpaCustomerFileReader(PathResource("")))
            // .writer(jpaCustomerWriter())
            .writer(repositoryCustomerWriter())
            .build()
    }

    @Bean
    @StepScope
    fun jpaCustomerFileReader(@Value("#{jobParameters['inputFile']}") inputFile: Resource): FlatFileItemReader<CustomerEntity> {
        return FlatFileItemReaderBuilder<CustomerEntity>()
            .name("jpaCustomerFileReader")
            .resource(inputFile)
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode")
            .fieldSetMapper {
                CustomerEntity(
                    firstName = it.readString("firstName"),
                    middleInitial = it.readString("middleInitial"),
                    lastName = it.readString("lastName"),
                    address = it.readString("address"),
                    city = it.readString("city"),
                    state = it.readString("state"),
                    zipCode = it.readString("zipCode"),
                )
            }.build()
    }

    @Bean
    fun repositoryCustomerWriter(): RepositoryItemWriter<CustomerEntity> {
        return RepositoryItemWriterBuilder<CustomerEntity>()
            .repository(customerRepository)
            .methodName("save")
            .build()
    }

    @Bean
    @StepScope
    fun jpaCustomerWriter(): JpaItemWriter<CustomerEntity> {
        return JpaItemWriterBuilder<CustomerEntity>()
            .entityManagerFactory(entityManagerFactory)
            .build()
    }
}