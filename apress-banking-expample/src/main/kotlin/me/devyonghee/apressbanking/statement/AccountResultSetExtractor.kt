package me.devyonghee.apressbanking.statement

import java.sql.ResultSet
import me.devyonghee.apressbanking.transaction.Transaction
import org.springframework.jdbc.core.ResultSetExtractor

class AccountResultSetExtractor : ResultSetExtractor<List<Account>> {

    private val accounts: MutableList<Account> = mutableListOf()
    private var curAccount: Account? = null

    override fun extractData(rs: ResultSet): List<Account> {
        while (rs.next()) {
            if (curAccount == null || curAccount?.id != rs.getLong("account_id")) {
                curAccount = Account(
                    id = rs.getLong("account_id"),
                    balance = rs.getBigDecimal("balance"),
                    lastStatementDate = rs.getDate("timestamp"),
                )
            }

            if (!rs.getString("description").isNullOrBlank()) {
                curAccount = curAccount?.addedTransaction(
                    Transaction(
                        transactionId = rs.getLong("transaction_id"),
                        accountId = rs.getLong("account_id"),
                        description = rs.getString("description"),
                        credit = rs.getBigDecimal("credit"),
                        debit = rs.getBigDecimal("debit"),
                        timestamp = rs.getDate("timestamp"),
                    )
                )
            }
            curAccount?.also { accounts.add(it) }
        }
        return accounts
    }
}