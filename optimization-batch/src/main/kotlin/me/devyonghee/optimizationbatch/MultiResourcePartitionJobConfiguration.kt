package me.devyonghee.optimizationbatch

import javax.sql.DataSource
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.partition.support.MultiResourcePartitioner
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

class MultiResourcePartitionJobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val multiThreadedWriter: ItemWriter<Transaction>,
) {
    @Bean
    fun multiResourcePartitionJob(): Job {
        return JobBuilder("multiResourcePartitionJob", jobRepository)
            .start(partitionedMaster())
            .build()
    }

    @Bean
    fun partitionedMaster(): Step {
        return StepBuilder("partitionedMaster", jobRepository)
            .partitioner(multiResourcePartitionStep1().name, partitioner(arrayOf()))
            .partitionHandler(multiResourcePartitionHandler())
            .build()
    }

    @Bean
    fun multiResourcePartitionHandler(): TaskExecutorPartitionHandler {
        return TaskExecutorPartitionHandler().apply {
            step = multiResourcePartitionStep1()
            setTaskExecutor(SimpleAsyncTaskExecutor())
        }
    }

    @Bean
    fun multiResourcePartitionStep1(): Step {
        return StepBuilder("multiStep1", jobRepository)
            .chunk<Transaction, Transaction>(100, transactionManager)
            .reader(multiResourcePartitionTransactionItemReader(PathResource("")))
            .writer(multiThreadedWriter)
            .build()
    }

    @Bean
    @StepScope
    fun multiResourcePartitionTransactionItemReader(
        // exeuction context 에서 파티션 특화 정보 가져오기
        @Value("#{stepExeuctionContext['file']}") resource: Resource,
    ): FlatFileItemReader<Transaction> {
        return FlatFileItemReaderBuilder<Transaction>()
            .name("multiResourcePartitionTransactionItemReader")
            .resource(resource)
            .delimited()
            .names("account", "amount", "timestamp")
            .fieldSetMapper {
                Transaction(
                    it.readString("account"),
                    it.readBigDecimal("amount"),
                    it.readDate("timestamp", "yyyy-MM-dd hh:mm:ss")
                )
            }.build()
    }

    @Bean
    fun partitioner(
        @Value("#{jobParameters['intputFiles']}") resources: Array<Resource>,
    ): MultiResourcePartitioner {
        return MultiResourcePartitioner().apply {
            setKeyName("file")
            setResources(resources)
        }
    }
}