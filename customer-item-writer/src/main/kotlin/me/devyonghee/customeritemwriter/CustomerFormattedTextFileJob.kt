package me.devyonghee.customeritemwriter

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class CustomerFormattedTextFileJob(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun formatJob(): Job {
        return JobBuilder("formatJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(formatStep())
            .build()
    }

    @Bean
    fun formatStep(): Step {
        return StepBuilder("formatStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(customerFileReader(PathResource("")))
            .writer(customerFileWriter(PathResource("")))
            .build()
    }

    @Bean
    @StepScope
    fun customerFileReader(@Value("#{jobParameters['inputFile']}") inputFile: Resource): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("customerFileReader")
            .resource(inputFile)
            .delimited()
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode")
            .fieldSetMapper {
                Customer(
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
    fun customerFileWriter(@Value("#{jobParameters['outputFile']}") outputFile: WritableResource): FlatFileItemWriter<Customer> {

        return FlatFileItemWriterBuilder<Customer>()
            .name("customerFileWriter")
            .resource(outputFile)
            // 형식화된 텍스트 파일
            // .formatted()
            // .format("%s %s lives at %s %s in %s, %s")

            // 구분자로 구분된 파일
            .delimited()
            .delimiter(";")

            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode")
            .build()
    }
}