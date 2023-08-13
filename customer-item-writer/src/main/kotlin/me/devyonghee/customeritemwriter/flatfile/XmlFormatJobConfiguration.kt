package me.devyonghee.customeritemwriter.flatfile

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.WritableResource
import org.springframework.oxm.xstream.XStreamMarshaller
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class XmlFormatJobConfiguration(
    private val jobRepository: JobRepository,
    private val customerFileReader: FlatFileItemReader<Customer>,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun xmlFormatJob(): Job {
        return JobBuilder("xmlFormatJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(xmlFormatStep())
            .build()
    }

    @Bean
    fun xmlFormatStep(): Step {
        return StepBuilder("xmlFormatStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(customerFileReader)
            .writer(xmlCustomerWriter(PathResource("")))
            .build()
    }

    @Bean
    @StepScope
    fun xmlCustomerWriter(@Value("#{jobParameters['outputFile']}") outputFile: WritableResource): StaxEventItemWriter<Customer> {
        return StaxEventItemWriterBuilder<Customer>()
            .name("xmlCustomerWriter")
            .resource(outputFile)
            .marshaller(XStreamMarshaller().apply {
                setAliases(mapOf("customer" to Customer::class.java))
                afterPropertiesSet()
            }).rootTagName("customers")
            .build()
    }
}