package me.devyonghee.customertransactionitemreader.mongodb

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.MongoItemReader
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class MongoTweetsJobConfiguration(
    private val jobRepository: JobRepository,
    private val mongoTemplate: MongoTemplate,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun mongodbJob(): Job {
        return JobBuilder("mongodbJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(mongodbStep())
            .build()
    }

    @Bean
    fun mongodbStep(): Step {
        return StepBuilder("mongodbStep", jobRepository)
            .chunk<Map<*, *>, Map<*, *>>(10, transactionManager)
            .reader(mongodbTweetsItemReader(""))
            .writer(itemWriter())
            .build()
    }

    @Bean
    @StepScope
    fun mongodbTweetsItemReader(
        @Value("#{jobParameters['hashTag']}") hashtag: String,
    ): MongoItemReader<Map<*, *>> {
        return MongoItemReaderBuilder<Map<*, *>>()
            .name("mongodbTweetsItemReader")
            .targetType(Map::class.java)
            .jsonQuery(
                """
                { "entities.hashtags.text": { ${"$"}eq: ?0 }}
            """.trimIndent()
            ).collection("tweets_collection")
            .parameterValues(listOf(hashtag))
            .pageSize(10)
            .sorts(mapOf("created_at" to Sort.Direction.ASC))
            .template(mongoTemplate)
            .build()
    }

    fun itemWriter(): ItemWriter<Map<*, *>> {
        return ItemWriter { items ->
            items.forEach { log.info("write tweets : {}", it) }
        }
    }
}