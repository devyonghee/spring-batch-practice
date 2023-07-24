package me.devyonghee.accounttransactionjob

import java.sql.Types
import javax.sql.DataSource
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate

class TransactionDaoSupport(
    dataSource: DataSource,
) : JdbcTemplate(dataSource) {

    fun getTransactionsByAccountNumber(accountNumber: String): List<Transaction> {
        return query(
            """
            select t.id, t.timestamp, t.amount 
                from transaction t 
                    join account_summary a on a.id = t.account_summary_id
            where t.account_number = ?
        """.trimIndent(),
            ArgumentTypePreparedStatementSetter(arrayOf(accountNumber), intArrayOf(Types.VARCHAR))
        ) { rs, _ ->
            Transaction(
                accountNumber = accountNumber,
                timestamp = rs.getDate("timestamp"),
                amount = rs.getDouble("amount"),
            )
        }
    }
}
