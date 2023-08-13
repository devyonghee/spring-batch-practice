package me.devyonghee.customeritemwriter.database

import java.sql.PreparedStatement
import javax.sql.DataSource
import me.devyonghee.customeritemwriter.flatfile.Customer
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.ItemPreparedStatementSetter
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JdbcFormatJobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val customerFileReader: FlatFileItemReader<Customer>,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun jdbcFormatJob(): Job {
        return JobBuilder("jdbcFormatJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(jdbcFormatStep())
            .build()
    }

    @Bean
    fun jdbcFormatStep(): Step {
        return StepBuilder("jdbcFormatStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(customerFileReader)
            .writer(jdbcCustomerWriter())
            .build()
    }

    @Bean
    @StepScope
    fun jdbcCustomerWriter(): JdbcBatchItemWriter<Customer> {
        return JdbcBatchItemWriterBuilder<Customer>()
            .dataSource(dataSource)
            // 물음표(`?`)를 값의 플레이스 홀더로 사용
            //.sql(
            //    """
            //    INSERT INTO customer(
            //        first_name,
            //        middle_initial,
            //        last_name,
            //        address,
            //        city,
            //        state,
            //        zip_code)
            //    VALUES (?, ?, ?, ?, ?, ?, ?)""".trimIndent()
            //).itemPreparedStatementSetter(CustomerItemPreparedStatementSetter)

            // 네임드 파라미터를 플레이스 홀더로 사용
            .sql(
                """
                INSERT INTO customer(
                    first_name,
                    middle_initial,
                    last_name,
                    address,
                    city,
                    state,
                    zip_code)
                VALUES (
                    :firstName,
                    :middleInitial,
                    :lastName,
                    :address,
                    :city,
                    :state,
                    :zipCode
                )""".trimIndent()
            ).beanMapped()

            .build()
    }

    object CustomerItemPreparedStatementSetter : ItemPreparedStatementSetter<Customer> {
        override fun setValues(item: Customer, ps: PreparedStatement) {
            ps.setString(1, item.firstName)
            ps.setString(2, item.middleInitial)
            ps.setString(3, item.lastName)
            ps.setString(4, item.address)
            ps.setString(5, item.city)
            ps.setString(6, item.state)
            ps.setString(7, item.zipCode)
        }
    }
}