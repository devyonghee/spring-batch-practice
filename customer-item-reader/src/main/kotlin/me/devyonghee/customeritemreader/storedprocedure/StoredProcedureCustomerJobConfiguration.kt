package me.devyonghee.customeritemreader.storedprocedure

import javax.sql.DataSource
import me.devyonghee.customeritemreader.jdbc.Customer
import me.devyonghee.customeritemreader.jdbc.CustomerRowMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.StoredProcedureItemReader
import org.springframework.batch.item.database.builder.StoredProcedureItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter
import org.springframework.jdbc.core.SqlParameter
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class StoredProcedureCustomerJobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 다음 쿼리를 미리 실행하여 프로시저를 생성해야 함
     *
     * DELIMITER //
     *
     * CREATE PROCEDURE customer_list(IN cityOption CHAR(16))
     * BEGIN
     * SELECT id, first_name, middle_initial, last_name, address, city, state, zip_code
     * FROM customer
     * WHERE city = cityOption;
     * END //
     *
     * DELIMITER ;
     */
    @Bean
    fun storedProcedureJob(): Job {
        return JobBuilder("storedProcedureJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(storedProcedureStep())
            .build()
    }

    @Bean
    fun storedProcedureStep(): Step {
        return StepBuilder("storedProcedureStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
            .reader(storedProcedureCursorItemReader(""))
            .writer(itemWriter())
            .build()
    }

    @Bean
    @StepScope
    fun storedProcedureCursorItemReader(
        @Value("#{jobParameters['city']}") city: String,
    ): StoredProcedureItemReader<Customer> {
        return StoredProcedureItemReaderBuilder<Customer>()
            .name("storedProcedureCursorItemReader")
            .dataSource(dataSource)
            .procedureName("customer_list")
            .parameters(SqlParameter("cityOption", java.sql.Types.VARCHAR))
            .preparedStatementSetter(
                ArgumentPreparedStatementSetter(arrayOf(city))
            ).rowMapper(CustomerRowMapper)
            .build()
    }

    fun itemWriter(): ItemWriter<Customer> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }
}