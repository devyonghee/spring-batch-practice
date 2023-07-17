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
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.file.mapping.PassThroughLineMapper
import org.springframework.batch.item.file.transform.PassThroughLineAggregator
import org.springframework.batch.repeat.CompletionPolicy
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.core.io.WritableResource
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
            .next(chunkStep())
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

    @Bean
    // CallableTaskletAdapter 를 사용한 Tasklet
    fun callableTasklet(): CallableTaskletAdapter {
        return CallableTaskletAdapter().apply {
            setCallable {
                log.info("This waw executed in another thread")
                RepeatStatus.FINISHED
            }
        }
    }

    @Bean
    @StepScope
    // MethodInvokingTaskletAdapter 를 사용한 Tasklet
    fun methodInvokingTaskletAdapter(
        @Value("#{jobParameters['name']}") name: String,
    ): MethodInvokingTaskletAdapter {
        return MethodInvokingTaskletAdapter().apply {
            setTargetObject(AnyService())
            setTargetMethod("anyMethod")
            setArguments(arrayOf(name))
        }
    }

    class AnyService {

        fun anyMethod(argument: String) {
            println("service method was called. argument(`$argument`)")
        }
    }

    @Bean
    // MethodInvokingTaskletAdapter 를 사용한 Tasklet
    fun systemCommandTasklet(): SystemCommandTasklet {
        return SystemCommandTasklet().apply {
            setCommand("echo 1")
            setTimeout(5000)
            setInterruptOnCancel(true)
        }
    }

    @Bean
    // chunk 방식
    fun chunkStep(): Step {
        return StepBuilder("step2", jobRepository)
            .chunk<String, String>(10, transactionManager)
//            .chunk<String, String>(completionPolicy(), transactionManager)
            .reader(itemReader(InputStreamResource(System.`in`)))
            .writer(itemWriter((FileSystemResource("output.txt"))))
            .build()
    }

    @Bean
    @StepScope
    fun itemReader(
        @Value("#{jobParameters['inputFile']}") inputFile: Resource,
    ): FlatFileItemReader<String> {
        return FlatFileItemReaderBuilder<String>()
            .name("itemReader")
            .resource(inputFile)
            .lineMapper(PassThroughLineMapper())
            .build()
    }

    @Bean
    @StepScope
    fun itemWriter(
        @Value("#{jobParameters['outputFile']}") outputFile: WritableResource,
    ): FlatFileItemWriter<String> {
        return FlatFileItemWriterBuilder<String>()
            .name("itemWriter")
            .resource(outputFile)
            .lineAggregator(PassThroughLineAggregator())
            .build()
    }

    @Bean
    fun completionPolicy(): CompletionPolicy {
        return CompositeCompletionPolicy().apply {
            setPolicies(
                arrayOf(
                    TimeoutTerminationPolicy(3),
                    SimpleCompletionPolicy(1000)
                )
            )
        }
    }
}