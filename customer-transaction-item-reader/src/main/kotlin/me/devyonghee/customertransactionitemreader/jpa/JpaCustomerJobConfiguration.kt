package me.devyonghee.customertransactionitemreader.jpa

import jakarta.persistence.EntityManagerFactory
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
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JpaCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val customerRepository: CustomerRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun jpaJob(): Job {
        return JobBuilder("jpaJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(jpaStep())
            .build()
    }

    @Bean
    fun jpaStep(): Step {
        return StepBuilder("jpaStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            //.reader(jpaCursorItemReader(""))
            .reader(springDataJpaItemReader(""))
            .writer(itemWriter())
            .build()
    }

    @Bean
    @StepScope
    fun springDataJpaItemReader(
        @Value("#{jobParameters['city']}") city: String,
    ): RepositoryItemReader<Customer> {
        return RepositoryItemReaderBuilder<Customer>()
            .name("springDataJpaItemReader")
            .repository(customerRepository)
            .methodName("findByCity")
            .arguments(listOf(city))
            .sorts(mapOf("lastName" to Sort.Direction.ASC))
            .build()
    }

    @Bean
    @StepScope
    fun jpaCursorItemReader(
        @Value("#{jobParameters['city']}") city: String,
    ): JpaCursorItemReader<Customer> {
        return JpaCursorItemReaderBuilder<Customer>()
            .name("jpaCursorItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryProvider(CustomerByCityQueryProvider(city))
            // .queryString(
            //     """
            //     SELECT c
            //     FROM Customer c
            //     WHERE c.city = :city
            //     """
            // ).parameterValues(mapOf("city" to city))
            .build()
    }

    fun itemWriter(): ItemWriter<Customer> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }
}