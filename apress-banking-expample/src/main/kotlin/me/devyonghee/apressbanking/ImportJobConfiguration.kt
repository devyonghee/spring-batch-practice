package me.devyonghee.apressbanking

import javax.sql.DataSource
import me.devyonghee.apressbanking.customer.Customer
import me.devyonghee.apressbanking.customer.CustomerAddressUpdate
import me.devyonghee.apressbanking.customer.CustomerContactUpdate
import me.devyonghee.apressbanking.customer.CustomerNameUpdate
import me.devyonghee.apressbanking.customer.CustomerUpdate
import me.devyonghee.apressbanking.customer.CustomerUpdateClassifier
import me.devyonghee.apressbanking.statement.AccountItemProcessor
import me.devyonghee.apressbanking.statement.Statement
import me.devyonghee.apressbanking.transaction.Transaction
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.LineTokenizer
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer
import org.springframework.batch.item.support.ClassifierCompositeItemWriter
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder
import org.springframework.batch.item.validator.ValidatingItemProcessor
import org.springframework.batch.item.xml.StaxEventItemReader
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ImportJobConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
    private val accountItemProcessor: AccountItemProcessor,
    private val transactionManager: PlatformTransactionManager,
) {
    @Bean
    fun importJob(): Job {
        return JobBuilder("importJob", jobRepository)
            .start(importCustomerUpdates())
            .next(importTransactions())
            .next(applyTransactions())
            .next()
            .build()
    }

    @Bean
    fun importCustomerUpdates(): Step {
        return StepBuilder("importCustomerUpdates", jobRepository)
            .chunk<CustomerUpdate, CustomerUpdate>(100, transactionManager)
            .reader(customerUpdateItemReader(PathResource("")))
            .processor(customerValidatingItemProcessor())
            .writer(customerUpdateItemWriter())
            .build()
    }

    @Bean
    @StepScope
    fun customerUpdateItemReader(
        @Value("#{jobParameters['customerUpdateFile']}") inputFile: Resource,
    ): FlatFileItemReader<CustomerUpdate> {
        return FlatFileItemReaderBuilder<CustomerUpdate>()
            .name("customerUpdateItemReader")
            .resource(inputFile)
            .lineTokenizer(customerUpdatesLineTokenizer())
            .fieldSetMapper(customerUpdateFieldsMapper())
            .build()
    }

    @Bean
    fun customerUpdatesLineTokenizer(): LineTokenizer {
        return PatternMatchingCompositeLineTokenizer().apply {
            setTokenizers(
                mapOf(
                    "1*" to DelimitedLineTokenizer().apply {
                        setNames("recordId", "customerId", "firstName", "middleName", "lastName")
                        afterPropertiesSet()
                    },
                    "2*" to DelimitedLineTokenizer().apply {
                        setNames("recordId", "customerId", "address1", "address2", "city", "state", "postalCode")
                        afterPropertiesSet()
                    },
                    "3*" to DelimitedLineTokenizer().apply {
                        setNames(
                            "recordId",
                            "customerId",
                            "emailAddress",
                            "homePhone",
                            "cellPhone",
                            "workPhone",
                            "notificationPreference"
                        )
                        afterPropertiesSet()
                    }
                )
            )
        }
    }

    @Bean
    fun customerUpdateItemWriter(): ClassifierCompositeItemWriter<CustomerUpdate> {
        return ClassifierCompositeItemWriterBuilder<CustomerUpdate>()
            .classifier(
                CustomerUpdateClassifier(
                    customerNameUpdateItemWriter(),
                    customerAddressUpdateItemWriter(),
                    customerContactUpdateItemWriter()
                )
            ).build()
    }

    @Bean
    fun customerUpdateFieldsMapper(): FieldSetMapper<CustomerUpdate> {
        return FieldSetMapper {
            when (it.readInt("recordId")) {
                1 -> CustomerNameUpdate(
                    customerId = it.readLong("customerId"),
                    firstName = it.readString("firstName"),
                    middleName = it.readString("middleName"),
                    lastName = it.readString("lastName"),
                )

                2 -> CustomerAddressUpdate(
                    customerId = it.readLong("customerId"),
                    address1 = it.readString("address1"),
                    address2 = it.readString("address2"),
                    city = it.readString("city"),
                    state = it.readString("state"),
                    postalCode = it.readString("postalCode"),
                )

                3 -> CustomerContactUpdate(
                    customerId = it.readLong("customerId"),
                    emailAddress = it.readString("emailAddress"),
                    homePhone = it.readString("homePhone"),
                    cellPhone = it.readString("cellPhone"),
                    workPhone = it.readString("workPhone"),
                    it.readString("notificationPreference").toIntOrNull()
                )

                else -> throw IllegalArgumentException("invalid record type was found: ${it.readInt("recordId")}")
            }
        }
    }

    @Bean
    fun customerValidatingItemProcessor(): ValidatingItemProcessor<CustomerUpdate> {
        return ValidatingItemProcessor<CustomerUpdate>().apply {
            setFilter(true)
        }
    }

    @Bean
    fun customerNameUpdateItemWriter(): JdbcBatchItemWriter<CustomerUpdate> {
        return JdbcBatchItemWriterBuilder<CustomerUpdate>()
            .beanMapped()
            .sql(
                """
                UPDATE CUSTOMER SET
                    first_name = COALESCE(:firstName, first_name),
                    middle_name = COALESCE(:middleName, middle_name),
                    last_name = COALESCE(:lastName, last_name)
                WHERE customer_id = :customerId
            """.trimIndent()
            ).dataSource(dataSource)
            .build()
    }

    @Bean
    fun customerAddressUpdateItemWriter(): JdbcBatchItemWriter<CustomerUpdate> {
        return JdbcBatchItemWriterBuilder<CustomerUpdate>()
            .beanMapped()
            .sql(
                """
                UPDATE CUSTOMER SET
                    address1 = COALESCE(:address1, address1),
                    address2 = COALESCE(:address2, address2),
                    city = COALESCE(:city, city),
                    state = COALESCE(:state, state),
                    postal_code = COALESCE(:postalCode, postal_code),
                WHERE customer_id = :customerId
            """.trimIndent()
            ).dataSource(dataSource)
            .build()
    }

    @Bean
    fun customerContactUpdateItemWriter(): JdbcBatchItemWriter<CustomerUpdate> {
        return JdbcBatchItemWriterBuilder<CustomerUpdate>()
            .beanMapped()
            .sql(
                """
                UPDATE CUSTOMER SET
                    email_address = COALESCE(:emailAddress, email_address),
                    home_phone = COALESCE(:homePhone, home_phone),
                    cell_phone = COALESCE(:cellPhone, cell_phone),
                    work_phone = COALESCE(:workPhone, work_phone),
                    notification_preference = COALESCE(:notificationPreference, notification_preference),
                WHERE customer_id = :customerId
            """.trimIndent()
            ).dataSource(dataSource)
            .build()
    }

    @Bean
    fun importTransactions(): Step {
        return StepBuilder("importTransactions", jobRepository)
            .chunk<Transaction, Transaction>(100, transactionManager)
            .reader(transactionItemReader(PathResource("")))
            .writer(transactionItemWriter())
            .build()
    }

    @Bean
    @StepScope
    fun transactionItemReader(@Value("#{jobParameters['transactionFile']}") transactionFile: Resource): StaxEventItemReader<Transaction> {
        return StaxEventItemReaderBuilder<Transaction>()
            .name("fooReader")
            .resource(transactionFile)
            .addFragmentRootElements("transaction")
            .unmarshaller(Jaxb2Marshaller().apply {
                setClassesToBeBound(Transaction::class.java)
            }).build()
    }

    @Bean
    fun transactionItemWriter(): JdbcBatchItemWriter<Transaction> {
        return JdbcBatchItemWriterBuilder<Transaction>()
            .dataSource(dataSource)
            .sql(
                """
                INSERT INTO TRANSACTION (
                    transaction_id,
                    account_id,
                    description,
                    credit,
                    debit,
                    timestamp
                ) VALUES (
                    :transactionId,
                    :accountId,
                    :description,
                    :credit,
                    :debit,
                    :timestamp
                )
            """.trimIndent()
            ).beanMapped()
            .build()
    }

    @Bean
    fun applyTransactions(): Step {
        return StepBuilder("applyTransactions", jobRepository)
            .chunk<Transaction, Transaction>(100, transactionManager)
            .reader(applyTransactionReader())
            .writer(applyTransactionWriter())
            .build()
    }

    @Bean
    fun applyTransactionReader(): JdbcCursorItemReader<Transaction> {
        return JdbcCursorItemReader<Transaction>().apply {
            dataSource = dataSource
            sql = """
                        SELECT
                            transaction_id,
                            account_id,
                            description,
                            credit,
                            debit,
                            timestamp
                        FROM transaction
                        ORDER BY timestamp
                    """.trimIndent()
            setRowMapper { rs, _ ->
                Transaction(
                    transactionId = rs.getLong("transaction_id"),
                    accountId = rs.getLong("account_id"),
                    description = rs.getString("description"),
                    credit = rs.getBigDecimal("credit"),
                    debit = rs.getBigDecimal("debit"),
                    timestamp = rs.getTimestamp("timestamp"),
                )
            }
        }
    }

    @Bean
    fun applyTransactionWriter(): JdbcBatchItemWriter<Transaction> {
        return JdbcBatchItemWriterBuilder<Transaction>()
            .dataSource(dataSource)
            .sql(
                """
                UPDATE ACCOUNT 
                    SET balance = balance + :transactionAmount
                WHERE account_id = :accountId
            """.trimIndent()
            ).beanMapped()
            .build()
    }

    @Bean
    fun generateStatements(): Step {
        return StepBuilder("generateStatements", jobRepository)
            .chunk<Statement, Statement>(1, transactionManager)
            .reader(statementItemReader())
            .processor(accountItemProcessor)
            .writer()
            .build()
    }

    @Bean
    fun statementItemReader(): JdbcCursorItemReader<Statement> {
        return JdbcCursorItemReaderBuilder<Statement>()
            .name("statementItemReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM CUSTOMER")
            .rowMapper { rs, _ ->
                Statement(
                    Customer(
                        rs.getLong("customer_id"),
                        rs.getString("first_name"),
                        rs.getString("middleName"),
                        rs.getString("lastName"),
                        rs.getString("address1"),
                        rs.getString("address2"),
                        rs.getString("city"),
                        rs.getString("state"),
                        rs.getString("postalCode"),
                        rs.getString("ssn"),
                        rs.getString("emailAddress"),
                        rs.getString("homePhone"),
                        rs.getString("cellPhone"),
                        rs.getString("workPhone"),
                        rs.getInt("notificationPreference"),
                    )
                )
            }.build()
    }
}
