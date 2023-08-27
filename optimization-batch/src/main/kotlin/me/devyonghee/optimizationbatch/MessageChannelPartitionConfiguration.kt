package me.devyonghee.optimizationbatch

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.partition.support.MultiResourcePartitioner
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.transaction.PlatformTransactionManager

@Profile("master")
@Configuration
@EnableBatchIntegration
class MessageChannelPartitionConfiguration(
    private val amqpTemplate: AmqpTemplate,
    private val jobRepository: JobRepository,
    private val connectionFactory: ConnectionFactory,
    private val masterStepBuilderFactory: RemotePartitioningManagerStepBuilderFactory,
) {
    @Bean
    fun remotePartitioningJob(): Job {
        return JobBuilder("remotePartitioningJob", jobRepository)
            .start(masterStep())
            .build()
    }

    @Bean
    @StepScope
    fun messageChannelPartitioner(
        @Value("#{jobParameters['inputFiles']}") resources: Array<Resource>,
    ): MultiResourcePartitioner {
        return MultiResourcePartitioner().apply {
            setKeyName("file")
            setResources(resources)
        }
    }

    @Bean
    fun masterStep(): Step {
        return masterStepBuilderFactory.get("masterStep")
            .partitioner("workerStep", messageChannelPartitioner(arrayOf()))
            .outputChannel(masterRequests())
            .inputChannel(masterReplies())
            .build()
    }

    @Bean
    fun masterOutboundFlow(): IntegrationFlow {
        return IntegrationFlow.from(masterRequests())
            .handle(Amqp.outboundAdapter(amqpTemplate).routingKey("requests"))
            .get()
    }

    @Bean
    fun masterRequests(): DirectChannel {
        return DirectChannel()
    }

    @Bean
    fun masterInboundFlow(): IntegrationFlow {
        return IntegrationFlow.from(Amqp.inboundAdapter(connectionFactory, "replies"))
            .channel(masterReplies())
            .get()
    }

    @Bean
    fun masterReplies(): DirectChannel {
        return DirectChannel()
    }

    @Profile("worker")
    @Configuration
    class WorkerConfiguration(
        private val connectionFactory: ConnectionFactory,
        private val transactionManager: PlatformTransactionManager,
        private val transactionItemReader: ItemReader<Transaction>,
        private val multiThreadedWriter: ItemWriter<Transaction>,
        private val workerStepBuilderFactory: RemotePartitioningWorkerStepBuilderFactory,
    ) {
        @Bean
        fun workerStep(): Step {
            return workerStepBuilderFactory.get("workerStep")
                .inputChannel(workerRequests())
                .outputChannel(workerReplies())
                .chunk<Transaction, Transaction>(100, transactionManager)
                .reader(transactionItemReader)
                .writer(multiThreadedWriter)
                .build()
        }

        @Bean
        fun workerInboundFlow(): IntegrationFlow {
            return IntegrationFlow.from(Amqp.inboundAdapter(connectionFactory, "requests"))
                .channel(workerRequests())
                .get()
        }

        @Bean
        fun workerRequests(): DirectChannel {
            return DirectChannel()
        }

        @Bean
        fun workerReplies(): DirectChannel {
            return DirectChannel()
        }
    }
}