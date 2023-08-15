package me.devyonghee.customeritemwriter.mongodb

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.MongoItemWriter
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class MongoCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val mongoTemplate: MongoTemplate,
) {
    @Bean
    fun mongoFormatJob(): Job {
        return JobBuilder("mongoFormatJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(mongoFormatStep())
            .build()
    }

    @Bean
    fun mongoFormatStep(): Step {
        return StepBuilder("mongoFormatStep", jobRepository)
            .chunk<CustomerMongo, CustomerMongo>(10, transactionManager)
            .reader(mongoCustomerFileReader(PathResource("")))
            .writer(mongoCustomerWriter())
            .build()
    }

    @Bean
    @StepScope
    fun mongoCustomerFileReader(@Value("#{jobParameters['inputFile']}") inputFile: Resource): FlatFileItemReader<CustomerMongo> {
        return FlatFileItemReaderBuilder<CustomerMongo>()
            .name("mongoCustomerFileReader")
            .resource(inputFile)
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode")
            .fieldSetMapper {
                CustomerMongo(
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
    @StepScope
    fun mongoCustomerWriter(): MongoItemWriter<CustomerMongo> {
        return MongoItemWriterBuilder<CustomerMongo>()
            .collection("customers")
            .template(mongoTemplate)
            .build()
    }
}