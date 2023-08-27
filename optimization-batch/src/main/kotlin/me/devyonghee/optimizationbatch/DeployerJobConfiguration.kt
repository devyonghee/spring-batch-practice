package me.devyonghee.optimizationbatch

import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.cloud.deployer.spi.task.TaskLauncher
import org.springframework.cloud.task.batch.partition.DeployerPartitionHandler
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler
import org.springframework.cloud.task.batch.partition.PassThroughCommandLineArgsProvider
import org.springframework.cloud.task.batch.partition.SimpleEnvironmentVariablesProvider
import org.springframework.cloud.task.repository.TaskRepository
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.io.Resource

@Configuration
class DeployerJobConfiguration(
    private val jobRepository: JobRepository,
    private val taskRepository: TaskRepository,
    private val context: ConfigurableApplicationContext,
    private val taskLauncher: TaskLauncher,
    private val applicationContext: ApplicationContext,
    private val jobExplorer: JobExplorer,
    private val environment: Environment,
) {
    @Bean
    @Profile("master")
    fun deployerPartitionHandler(): DeployerPartitionHandler {
        val resource: Resource =
            applicationContext.getResource("file:///optimization-batch/build/libs/optimization-batch-0.0.1-SNAPSHOT.jar")

        return DeployerPartitionHandler(taskLauncher, jobExplorer, resource, "step1Async", taskRepository)
            .apply {
                setEnvironmentVariablesProvider(SimpleEnvironmentVariablesProvider(environment))
                setMaxWorkers(3)
                setApplicationName("deployerPartitionHandler")
                setCommandLineArgsProvider(
                    PassThroughCommandLineArgsProvider(
                        listOf(
                            "--spring.profiles.active=worker",
                            "--spring.cloud.task.initialize.enabled=false",
                            "--spring.batch.initializer.enabled=false",
                            "--spring.datasource.initializer=false",
                        )
                    )
                )
            }
    }

    @Bean
    @Profile("worker")
    fun deployerStepExecutionHandler(): DeployerStepExecutionHandler {
        return DeployerStepExecutionHandler(context, jobExplorer, jobRepository)
    }
}