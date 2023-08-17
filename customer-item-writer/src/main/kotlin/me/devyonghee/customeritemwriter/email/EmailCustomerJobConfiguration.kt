package me.devyonghee.customeritemwriter.email

import me.devyonghee.customeritemwriter.jpa.CustomerEntity
import me.devyonghee.customeritemwriter.jpa.CustomerRepository
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.data.RepositoryItemReader
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder
import org.springframework.batch.item.mail.SimpleMailMessageItemWriter
import org.springframework.batch.item.mail.builder.SimpleMailMessageItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Sort
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class EmailCustomerJobConfiguration(
    private val jpaFormatStep: Step,
    private val jobRepository: JobRepository,
    private val customerRepository: CustomerRepository,
    private val transactionManager: PlatformTransactionManager,
    private val mailSender: MailSender,
) {
    @Bean
    fun emailJob(): Job {
        return JobBuilder("emailCustomerJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(jpaFormatStep)
            .next(emailStep())
            .build()
    }

    @Bean
    fun emailStep(): Step {
        return StepBuilder("emailCustomerStep", jobRepository)
            .chunk<CustomerEntity, SimpleMailMessage>(10, transactionManager)
            .reader(springDataJpaItemReader())
            .processor {
                SimpleMailMessage().apply {
                    from = "devyonghee@gmail.com"
                    setTo(it.email)
                    subject = "Welcome!"
                    text = "Welcome ${it.firstName} ${it.lastName}!"
                }
            }
            .writer(emailCustomerWriter())
            .build()
    }

    @Bean
    fun emailCustomerWriter(): SimpleMailMessageItemWriter {
        return SimpleMailMessageItemWriterBuilder()
            .mailSender(mailSender)
            .build()
    }

    @Bean
    @StepScope
    fun springDataJpaItemReader(): RepositoryItemReader<CustomerEntity> {
        return RepositoryItemReaderBuilder<CustomerEntity>()
            .name("springDataJpaItemReader")
            .repository(customerRepository)
            .methodName("findAll")
            .sorts(mapOf("lastName" to Sort.Direction.ASC))
            .maxItemCount(1)
            .build()
    }
}