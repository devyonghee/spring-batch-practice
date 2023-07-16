package me.devyonghee.simplejobstep

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersValidator
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.CompositeJobParametersValidator
import org.springframework.batch.core.job.DefaultJobParametersValidator
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.listener.ExecutionContextPromotionListener
import org.springframework.batch.core.listener.JobListenerFactoryBean
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
            .next(step2())
            .validator(validator())
//            .incrementer(RunIdIncrementer())
            .incrementer(DailyJobTimestamper)
//            .listener(JobLoggerListener)
            .listener(JobListenerFactoryBean.getListener(AnnotationJobLoggerListener))
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
            .tasklet(helloWorldTasklet("", ""), transactionManager)
            .listener(promotionListener())
            .build()
    }

    @Bean
    fun step2(): Step {
        return StepBuilder("step2", jobRepository)
            .tasklet(goodByeTasklet(), transactionManager)
            .build()
    }

    // 늦은 바인딩 방식
    @Bean
    @StepScope
    fun helloWorldTasklet(
        @Value("#{jobParameters['name']}") name: String,
        @Value("#{jobParameters['fileName']}") fileName: String,
    ): Tasklet {
        return Tasklet { _: StepContribution, context: ChunkContext ->
            context.stepContext.stepExecution.executionContext.put(
                "user.name", context.stepContext.jobParameters["name"]
            )
            log.info("hello, {}!", name)
            log.info("fileName={}", fileName)
            RepeatStatus.FINISHED
        }
    }

    @Bean
    @StepScope
    fun goodByeTasklet(): Tasklet {
        return Tasklet { _: StepContribution, context: ChunkContext ->
            log.info("bye, {}!", context.stepContext.jobExecutionContext["user.name"])
            RepeatStatus.FINISHED
        }
    }

    @Bean
    fun validator(): JobParametersValidator {
        val defaultValidator: JobParametersValidator =
            DefaultJobParametersValidator(arrayOf("fileName"), arrayOf("name", "currentDate")).apply {
                afterPropertiesSet()
            }
        return CompositeJobParametersValidator().apply {
            setValidators(listOf(defaultValidator, CustomParameterValidator))
        }
    }

    @Bean
    fun promotionListener(): StepExecutionListener {
        // step 에서 name 키를 찾으면 잡의 ExecutionContext 에 복사
        return ExecutionContextPromotionListener().apply {
            setKeys(arrayOf("user.name"))
        }
    }
}