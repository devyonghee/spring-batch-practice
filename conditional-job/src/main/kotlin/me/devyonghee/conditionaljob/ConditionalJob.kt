package me.devyonghee.conditionaljob

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ConditionalJob(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun job(): Job {
        return JobBuilder("conditionalJob", jobRepository)
            .start(conditionalFirstStep())
            .next(RandomDecider)
            // FAILED 로 잡이 종료되지만, 재구동하면 successStep 에서부터 재실행
            .from(RandomDecider).on(ExitStatus.FAILED.exitCode).stopAndRestart(successStep())
            .from(RandomDecider).on("*").to(successStep())
            .end()
            .build()
    }

    @Bean
    fun conditionalFirstStep(): Step {
        return StepBuilder("conditionalFirstStep", jobRepository)
            .tasklet(passTasklet(), transactionManager)
            .build()
    }

    @Bean
    fun successStep(): Step {
        return StepBuilder("successStep", jobRepository)
            .tasklet(successTasklet(), transactionManager)
            .build()
    }

    fun passTasklet(): Tasklet {
        return Tasklet { _: StepContribution, _: ChunkContext ->
            RepeatStatus.FINISHED
            // throw RuntimeException("This is a failure")
        }
    }

    fun successTasklet(): Tasklet {
        return Tasklet { _: StepContribution, _: ChunkContext ->
            log.info("Success!")
            RepeatStatus.FINISHED
        }
    }
}