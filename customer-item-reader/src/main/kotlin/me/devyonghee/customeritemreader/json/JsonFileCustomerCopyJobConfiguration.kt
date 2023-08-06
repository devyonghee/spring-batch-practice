package me.devyonghee.customeritemreader.json

import com.fasterxml.jackson.databind.ObjectMapper
import java.text.SimpleDateFormat
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
import org.springframework.batch.item.json.JacksonJsonObjectReader
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JsonFileCustomerCopyJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun jsonFileJob(): Job {
        return JobBuilder("jsonFileJob", jobRepository)
            .start(jsonFileCopyFileStep())
            .build()
    }

    @Bean
    fun jsonFileCopyFileStep(): Step {
        return StepBuilder("jsonFileCopyFileStep", jobRepository)
            .chunk<CustomerJson, CustomerJson>(10, transactionManager)
            .reader(jsonFileCustomerReader(ClassPathResource("")))
            .writer(itemWriter())
            .build()
    }

    fun itemWriter(): ItemWriter<CustomerJson> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }

    @Bean
    @StepScope
    fun jsonFileCustomerReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource,
    ): ItemStreamReader<CustomerJson> {
        return JsonItemReaderBuilder<CustomerJson>()
            .name("jsonFileCustomerReader")
            .resource(inputFile)
            .jsonObjectReader(JacksonJsonObjectReader(CustomerJson::class.java)
                .apply {
                    setMapper(ObjectMapper().apply {
                        setDateFormat(SimpleDateFormat("yyyy-MM-dd hh:mm:ss"))
                    })
                })
            .build()
    }
}