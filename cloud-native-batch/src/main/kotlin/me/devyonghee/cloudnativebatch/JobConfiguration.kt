package me.devyonghee.cloudnativebatch

import javax.sql.DataSource
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecutionListener
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.MultiResourceItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.web.client.RestTemplate

@Configuration
class JobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun job(jobExecutionListener: JobExecutionListener): Job {
        return JobBuilder("s3jdbc", jobRepository)
            .listener(jobExecutionListener)
            .start(load())
            .build()
    }

    @Bean
    fun load(): Step {
        return StepBuilder("load", jobRepository)
            .chunk<Foo, Foo>(20, transactionManager)
            .reader(multiResourceItemReader(""))
            .processor(processor())
            .writer(writer())
            .build()
    }

    @Bean
    @StepScope
    fun multiResourceItemReader(
        @Value("#{jobParameters['localFiles']}") paths: String,
    ): MultiResourceItemReader<Foo> {
        return MultiResourceItemReaderBuilder<Foo>()
            .name("multiReader")
            .delegate(delegate())
            .resources(
                *paths.split(",").map { PathResource(it) }.toTypedArray()
            ).build()
    }

    @Bean
    fun delegate(): FlatFileItemReader<Foo> {
        return FlatFileItemReaderBuilder<Foo>()
            .name("fooReader")
            .delimited()
            .names("first", "second", "third")
            .targetType(Foo::class.java)
            .build()
    }

    @Bean
    fun writer(): JdbcBatchItemWriter<Foo> {
        return JdbcBatchItemWriterBuilder<Foo>()
            .dataSource(dataSource)
            .beanMapped()
            .sql("INSERT INTO FOO VALUES (:first, :second, :third, :message)")
            .build()
    }

    @Bean
    @StepScope
    fun processor(): EnrichmentProcessor {
        return EnrichmentProcessor(restTemplate())
    }

    @Bean
    fun downloadingJobExecutionListener(
        resourcePatternResolver: ResourcePatternResolver,
        @Value("\${job.resource-path}") path: String,
    ): DownloadingJobExecutionListener {
        return DownloadingJobExecutionListener(resourcePatternResolver, path)
    }

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}