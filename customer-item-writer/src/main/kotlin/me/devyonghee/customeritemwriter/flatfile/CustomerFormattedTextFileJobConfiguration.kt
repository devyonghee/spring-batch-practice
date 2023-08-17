package me.devyonghee.customeritemwriter.flatfile

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
class CustomerFormattedTextFileJobConfiguration(
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
            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode", "email")
            .fieldSetMapper {
                Customer(
                    firstName = it.readString("firstName"),
                    middleInitial = it.readString("middleInitial"),
                    lastName = it.readString("lastName"),
                    address = it.readString("address"),
                    city = it.readString("city"),
                    state = it.readString("state"),
                    zipCode = it.readString("zipCode"),
                    email = it.readString("email")
                )
            }.build()
    }

    @Bean
    @StepScope
    fun customerFileWriter(@Value("#{jobParameters['outputFile']}") outputFile: WritableResource): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("customerFlatFileWriter")
            .resource(outputFile)
            // 형식화된 텍스트 파일
            // .formatted()
            // .format("%s %s lives at %s %s in %s, %s")

            // 구분자로 구분된 파일
            .delimited()
            .delimiter(";")

            .names("firstName", "middleInitial", "lastName", "address", "city", "state", "zipCode", "email")
            // 아이템이 기록되지 않은 경우 출력 파일 삭제
            .shouldDeleteIfEmpty(true)
            // 같은 파일의 이름이 이미 존재하는 경우 삭제하지 않고 보호
            .shouldDeleteIfExists(false)
            // 결과 파일이 이미 존재하면 기존 파일에 데이터 추가
            .append(true)
            .build()
    }
}