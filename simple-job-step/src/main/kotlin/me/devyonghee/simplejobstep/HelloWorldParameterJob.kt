package me.devyonghee.simplejobstep

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class HelloWorldParameterJob(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun job(): Job {
        return JobBuilder("basicJob", jobRepository)
            .start(step1())
            .build()
    }

//    @Bean
//    fun step1(): Step {
//        return StepBuilder("step1", jobRepository)
//            .tasklet(helloWorldTasklet(), transactionManager)
//            .build()
//    }
//
//    @Bean
//    fun helloWorldTasklet(): Tasklet {
//        return Tasklet { _: StepContribution, context: ChunkContext ->
//            val name: Any? = context.stepContext.jobParameters["name"]
//            log.info("hello, {}!", name)
//            RepeatStatus.FINISHED
//        }
//    }

    @Bean
    fun step1(): Step {
        return StepBuilder("step1", jobRepository)
            .tasklet(helloWorldTasklet(""), transactionManager)
            .build()
    }

    // 늦은 바인딩 방식
    @Bean
    @StepScope
    fun helloWorldTasklet(@Value("#{jobParameters['name']}") name: String): Tasklet {
        return Tasklet { _: StepContribution, _: ChunkContext ->
            log.info("hello, {}!", name)
            RepeatStatus.FINISHED
        }
    }
}