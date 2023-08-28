package me.devyonghee.optimizationbatch

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.integration.amqp.dsl.Amqp
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.dsl.IntegrationFlow

@Configuration
@EnableBatchIntegration
class RemoteChunkingJobConfiguration {

    @Configuration
    @Profile("master")
    class RemoteChunkingMasterConfiguration(
        private val amqpTemplate: AmqpTemplate,
        private val jobRepository: JobRepository,
        private val connectionFactory: ConnectionFactory,
        private val transactionItemReader: FlatFileItemReader<Transaction>,
        private val remoteChunkingManagerStepBuilderFactory: RemoteChunkingManagerStepBuilderFactory,
    ) {
        @Bean
        fun remoteChunkingJob(): Job {
            return JobBuilder("remoteChunkingJob", jobRepository)
                .start(remoteChunkingMasterStep())
                .build()
        }

        @Bean
        fun remoteChunkingMasterStep(): Step {
            return remoteChunkingManagerStepBuilderFactory.get<Transaction, Transaction>("remoteChunkingMasterStep")
                .chunk(100)
                .reader(transactionItemReader)
                .outputChannel(remoteChunkingMasterRequests())
                .inputChannel(remoteChunkingMasterReplies())
                .build()
        }

        @Bean
        fun remoteChunkingMasterRequests(): DirectChannel {
            return DirectChannel()
        }

        @Bean
        fun remoteChunkingMasterOutboundFlow(): IntegrationFlow {
            return IntegrationFlow.from(remoteChunkingMasterRequests())
                .handle(Amqp.outboundAdapter(amqpTemplate).routingKey("remoteChunkingRequests"))
                .get()
        }

        @Bean
        fun remoteChunkingMasterReplies(): QueueChannel {
            return QueueChannel()
        }

        @Bean
        fun remoteChunkingMasterInboundFlow(): IntegrationFlow {
            return IntegrationFlow.from(Amqp.inboundGateway(connectionFactory, "remoteChunkingReplies"))
                .channel(remoteChunkingMasterReplies())
                .get()
        }
    }

    @Configuration
    @Profile("master")
    class RemoteChunkingWorkerConfiguration(
        private val amqpTemplate: AmqpTemplate,
        private val workerBuilder: RemoteChunkingWorkerBuilder<Transaction, Transaction>,
        private val connectionFactory: ConnectionFactory,
        private val multiThreadedWriter: JdbcBatchItemWriter<Transaction>,
    ) {
        @Bean
        fun workerIntegrationFlow(): IntegrationFlow {
            return workerBuilder.itemProcessor {
                print("processing transaction = $it")
                it
            }.itemWriter(multiThreadedWriter)
                .inputChannel(remoteChunkingWorkerRequests())
                .outputChannel(remoteChunkingWorkerReplies())
                .build()
        }

        @Bean
        fun remoteChunkingWorkerOutboundFlow(): IntegrationFlow {
            return IntegrationFlow.from(remoteChunkingWorkerRequests())
                .handle(Amqp.outboundAdapter(amqpTemplate).routingKey("remoteChunkingRequests"))
                .get()
        }

        fun remoteChunkingWorkerRequests(): DirectChannel {
            return DirectChannel()
        }

        @Bean
        fun remoteChunkingWorkerInboundFlow(): IntegrationFlow {
            return IntegrationFlow.from(Amqp.inboundGateway(connectionFactory, "remoteChunkingReplies"))
                .channel(remoteChunkingWorkerReplies())
                .get()
        }

        fun remoteChunkingWorkerReplies(): DirectChannel {
            return DirectChannel()
        }
    }
}