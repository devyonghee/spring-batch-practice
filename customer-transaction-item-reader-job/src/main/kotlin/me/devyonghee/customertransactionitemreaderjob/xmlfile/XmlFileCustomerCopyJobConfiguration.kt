package me.devyonghee.customertransactionitemreaderjob.xmlfile

import me.devyonghee.customertransactionitemreaderjob.domain.Customer
import me.devyonghee.customertransactionitemreaderjob.domain.CustomerLineType
import me.devyonghee.customertransactionitemreaderjob.domain.Transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.oxm.Unmarshaller
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class XmlFileCustomerCopyJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun xmlFileJob(): Job {
        return JobBuilder("xmlFileJob", jobRepository)
            .start(xmlFileCopyFileStep())
            .build()
    }

    @Bean
    fun xmlFileCopyFileStep(): Step {
        return StepBuilder("xmlFileCopyFileStep", jobRepository)
            .chunk<CustomerLineType, CustomerLineType>(10, transactionManager)
            .reader(xmlFileCustomerReader(ClassPathResource("")))
            .writer(itemWriter())
            .build()
    }

    fun itemWriter(): ItemWriter<CustomerLineType> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }

    @Bean
    @StepScope
    fun xmlFileCustomerReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource,
    ): ItemStreamReader<Customer?> {
        return StaxEventItemReaderBuilder<Customer?>()
            .name("xmlFileCustomerReader")
            .resource(inputFile)
            .addFragmentRootElements("customer")
            .unmarshaller(customerMarshaller())
            .build()
    }

    @Bean
    fun customerMarshaller(): Unmarshaller {
        return Jaxb2Marshaller().apply {
            setClassesToBeBound(Customer::class.java, Transaction::class.java)
        }
    }
}