package me.devyonghee.databasecustomertransactionitemreader.jdbc

import javax.sql.DataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.PagingQueryProvider
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JdbcCustomerJobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @Bean
    fun jdbcJob(): Job {
        return JobBuilder("jdbcJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(jdbcStep())
            .build()
    }

    @Bean
    fun jdbcStep(): Step {
        return StepBuilder("jdbcStep", jobRepository)
            .chunk<Customer, Customer>(10, transactionManager)
//            .reader(jdbcCursorItemReader())
            .reader(jdbcPagingItemReader(pagingQueryProviderFactoryBean().`object`, ""))
            .writer(itemWriter())
            .build()
    }

    @Bean
    fun jdbcCursorItemReader(): JdbcCursorItemReader<Customer> {
        return JdbcCursorItemReaderBuilder<Customer>()
            .name("jdbcCursorItemReader")
            .dataSource(dataSource)
            .sql(
                """
                SELECT id, first_name, middle_initial, last_name, address, city, state, zip_code
                FROM customer
                WHERE city = ?
            """.trimIndent()
            ).rowMapper(CustomerRowMapper)
            .preparedStatementSetter(citySetter(""))
            .build()
    }

    @Bean
    @StepScope
    fun citySetter(
        @Value("#{jobParameters['city']}") city: String,
    ): ArgumentPreparedStatementSetter {
        return ArgumentPreparedStatementSetter(arrayOf(city))
    }

    fun itemWriter(): ItemWriter<Customer> {
        return ItemWriter { items ->
            items.forEach { log.info("write customer `{}`", it) }
        }
    }

    @Bean
    @StepScope
    fun jdbcPagingItemReader(
        queryProvider: PagingQueryProvider,
        @Value("#{jobParameters['city']}") city: String,
    ): ItemReader<Customer> {
        return JdbcPagingItemReaderBuilder<Customer>()
            .name("jdbcPagingItemReader")
            .dataSource(dataSource)
            .queryProvider(queryProvider)
            .parameterValues(mapOf("city" to city))
            .pageSize(10)
            .rowMapper(CustomerRowMapper)
            .build()
    }

    @Bean
    fun pagingQueryProviderFactoryBean(): SqlPagingQueryProviderFactoryBean {
        return SqlPagingQueryProviderFactoryBean().apply {
            setSelectClause("select id, first_name, middle_initial, last_name, address, city, state, zip_code")
            setFromClause("from customer")
            setWhereClause("where city = :city")
            setSortKey("last_name")
            setDataSource(dataSource)
        }
    }
}