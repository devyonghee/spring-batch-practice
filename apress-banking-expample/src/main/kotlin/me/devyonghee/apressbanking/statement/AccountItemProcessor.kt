package me.devyonghee.apressbanking.statement

import java.sql.Types
import org.springframework.batch.item.ItemProcessor
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class AccountItemProcessor(
    private val jdbcTemplate: JdbcTemplate,
) : ItemProcessor<Statement, Statement> {

    override fun process(item: Statement): Statement? {
        val accounts: List<Account> = jdbcTemplate.query(
            """
                SELECT
                    a.account_id,
                    a.balance,
                    a.last_statement_date,
                    t.transaction_id,
                    t.description,
                    t.credit,
                    t.debit,
                    t.timestamp 
                FROM account a
                    LEFT JOIN transaction t on a.account_id = t.account_id
                WHERE a.account_id in 
                    (select account_id from customer_account where customer_id = ?)
                ORDER BY t.timestamp
            """.trimIndent(),
            ArgumentTypePreparedStatementSetter(arrayOf(item.customer.id), intArrayOf(Types.BIGINT)),
            AccountResultSetExtractor(),
        ) ?: listOf()

        return item.changedAccounts(accounts)
    }
}