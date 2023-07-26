package me.devyonghee.flatfilecustomercopyjob

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.LineTokenizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.transaction.PlatformTransactionManager

@SpringBootApplication
class FlatFileCustomerCopyJobApplication(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun job(): Job {
        return JobBuilder("job", jobRepository)
            .start(copyFileStep())
            .build()
    }

    @Bean
    fun copyFileStep(): Step {
        return StepBuilder("copyFileStep", jobRepository)
            .chunk<CustomerLineType, CustomerLineType>(10, transactionManager)
            .reader(customerItemReader(PathResource("")))
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
    fun customerItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource,
    ): FlatFileItemReader<CustomerLineType> {
        return FlatFileItemReaderBuilder<CustomerLineType>()
            .name("customerItemReader")
            .resource(inputFile)
            // 구분자가 있는 경우
            //.delimited()
            // 고정된 길이인 경우
            //.fixedLength()
            //.columns(
            //    Range(1, 11),
            //    Range(12, 12),
            //    Range(13, 22),
            //    Range(23, 26),
            //    Range(27, 46),
            //    Range(47, 62),
            //    Range(63, 64),
            //    Range(65, 69),
            //)
            //.names(
            //    "firstName",
            //    "middleInitial",
            //    "lastName",
            //    "addressNumber",
            //    "street",
            //    "city",
            //    "state",
            //    "zipCode",
            //)
            // 커스텀 구분자를 사용하는 경우
            // .lineTokenizer(CustomerFileLineTokenizer)
            // .fieldSetMapper(CustomerFieldSetMapper())
            .lineMapper(lineTokenizer())
            .build()
    }

    @Bean
    fun lineTokenizer(): PatternMatchingCompositeLineMapper<CustomerLineType> {
        return PatternMatchingCompositeLineMapper<CustomerLineType>().apply {
            setTokenizers(
                mapOf(
                    "CUST*" to customerLineTokenizer(),
                    "TRANS*" to transactionLineTokenizer(),
                )
            )
            setFieldSetMappers(
                mapOf(
                    "CUST*" to CustomerFieldSetMapper,
                    "TRANS*" to TransactionFieldSetMapper,
                )
            )
        }
    }

    @Bean
    fun transactionLineTokenizer(): LineTokenizer {
        return DelimitedLineTokenizer().apply {
            setNames(
                "prefix",
                "accountNumber",
                "transactionDate",
                "amount",
            )
        }
    }

    @Bean
    fun customerLineTokenizer(): LineTokenizer {
        return DelimitedLineTokenizer().apply {
            setIncludedFields(*(1..8).toSet().toIntArray())
            setNames(
                "firstName",
                "middleInitial",
                "lastName",
                "addressNumber",
                "street",
                "city",
                "state",
                "zipCode",
            )
        }
    }
}


fun main(args: Array<String>) {
    runApplication<FlatFileCustomerCopyJobApplication>(*args)
}