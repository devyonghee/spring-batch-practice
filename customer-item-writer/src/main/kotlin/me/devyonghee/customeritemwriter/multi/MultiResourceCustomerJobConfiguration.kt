package me.devyonghee.customeritemwriter.multi

import me.devyonghee.customeritemwriter.flatfile.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.MultiResourceItemWriter
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.oxm.xstream.XStreamMarshaller
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class MultiResourceCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val itemReader: FlatFileItemReader<Customer>,
) {
    @Bean
    fun xmlGeneratorJob(): Job {
        return JobBuilder("multiXmlGeneratorJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(multiXmlGeneratorStep())
            .build()
    }

    @Bean
    fun multiXmlGeneratorStep(): Step {
        return StepBuilder("multiXmlGeneratorStep", jobRepository)
            .chunk<Customer, Customer>(2, transactionManager)
            .reader(itemReader)
            .writer(multiResourceItemWriter())
            .build()
    }

    @Bean
    fun multiResourceItemWriter(): MultiResourceItemWriter<Customer> {
        return MultiResourceItemWriterBuilder<Customer>()
            .name("multiResourceItemWriter")
            .delegate(multiXmlCustomerWriter())
            .resource(FileSystemResource("customer-item-writer/output/customer"))
            .resourceSuffixCreator { "$it.xml" }
            .itemCountLimitPerResource(4)
            .build()
    }

    @Bean
    @StepScope
    fun multiXmlCustomerWriter(): StaxEventItemWriter<Customer> {
        return StaxEventItemWriterBuilder<Customer>()
            .name("xmlCustomerWriter")
            .marshaller(XStreamMarshaller().apply {
                setAliases(mapOf("customer" to Customer::class.java))
                afterPropertiesSet()
            }).rootTagName("customers")
            .build()
    }

}