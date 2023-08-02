package me.devyonghee.databasecustomertransactionitemreader.jpa

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
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JpaCustomerJobConfiguration(
    private val jobRepository: JobRepository,
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
//            .reader(jpaCursorItemReader())
            .reader(jpaCursorItemReader(""))
            .writer(itemWriter())
            .build()
    }

    @Bean
    @StepScope
    fun jpaCursorItemReader(
        @Value("#{jobParameters['city']}") city: String,
    ): ItemReader<Customer> {
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