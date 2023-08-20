package me.devyonghee.customeritemwriter

import me.devyonghee.customeritemwriter.flatfile.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.support.ClassifierCompositeItemWriter
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.classify.Classifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ClassifierCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val customerFileReader: FlatFileItemReader<Customer>,
    private val xmlCustomerWriter: StaxEventItemWriter<Customer>,
    jdbcCustomerWriter: JdbcBatchItemWriter<Customer>,
) {
    private val customerClassifier: Classifier<Customer, ItemWriter<in Customer>> = CustomerClassifier(
        matchesItemWriter = xmlCustomerWriter,
        defaultItemWriter = jdbcCustomerWriter
    )

    @Bean
    fun classifierCustomerJob(): Job {
        return JobBuilder("classifierCustomerJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(classifierCustomerStep())
            .build()
    }

    @Bean
    fun classifierCustomerStep(): Step {
        return StepBuilder("classifierCustomerStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(customerFileReader)
            .writer(classifierWriter())
            .stream(xmlCustomerWriter)
            .build()
    }

    @Bean
    fun classifierWriter(): ClassifierCompositeItemWriter<Customer> {
        return ClassifierCompositeItemWriterBuilder<Customer>()
            .classifier(customerClassifier)
            .build()
    }

    private data class CustomerClassifier(
        private val matchesItemWriter: ItemWriter<Customer>,
        private val defaultItemWriter: ItemWriter<Customer>,
    ) : Classifier<Customer, ItemWriter<in Customer>> {

        override fun classify(customer: Customer): ItemWriter<Customer> {
            return if (customer.state.matches(Regex("^[A-M].*"))) {
                matchesItemWriter
            } else {
                defaultItemWriter
            }
        }
    }
}