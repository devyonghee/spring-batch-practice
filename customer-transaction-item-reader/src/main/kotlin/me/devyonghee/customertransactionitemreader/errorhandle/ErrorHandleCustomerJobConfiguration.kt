package me.devyonghee.customertransactionitemreader.errorhandle

import java.io.FileNotFoundException
import me.devyonghee.customertransactionitemreader.flatfile.CustomerFieldSetMapper
import me.devyonghee.customertransactionitemreader.flatfile.CustomerLineType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.skip.SkipPolicy
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.ParseException
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ErrorHandleCustomerJobConfiguration(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun errorHandleJob(): Job {
        return JobBuilder("errorHandleJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(errorHandleStep())
            .build()
    }

    @Bean
    fun errorHandleStep(): Step {
        return StepBuilder("errorHandleStep", jobRepository)
            .chunk<CustomerLineType, CustomerLineType>(10, transactionManager)
            .reader(errorHandleItemReader(PathResource("")))
            .writer(itemWriter())
            .faultTolerant()
            // .skip(Exception::class.java)
            // .noSkip(ParseException::class.java)
            // .skipLimit(10)
            .skipPolicy(FileVerificationSkipper)
            .listener(CustomerItemListener)
            .listener(EmptyInputStepFailListener)
            .build()
    }


    @Bean
    @StepScope
    fun errorHandleItemReader(
        @Value("#{jobParameters['customerFile']}") inputFile: Resource,
    ): FlatFileItemReader<CustomerLineType> {
        return FlatFileItemReaderBuilder<CustomerLineType>()
            .name("errorHandleItemReader")
            .resource(inputFile)
            .delimited()
            .names(
                "firstName",
                "middleInitial",
                "lastName",
                "addressNumber",
                "street",
                "city",
                "state",
                "zipCode",
            ).fieldSetMapper(CustomerFieldSetMapper)
            .build()
    }


    fun itemWriter(): ItemWriter<CustomerLineType> {
        return ItemWriter { items ->
            items.forEach { log.info("write errorHandle `{}`", it) }
        }
    }

    object FileVerificationSkipper : SkipPolicy {
        override fun shouldSkip(t: Throwable, skipCount: Long): Boolean {
            return when (t) {
                is FileNotFoundException -> false
                is ParseException -> skipCount <= 10
                else -> false
            }
        }
    }
}