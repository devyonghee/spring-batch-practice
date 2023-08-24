package me.devyonghee.apressbanking.statement

import java.math.BigDecimal
import java.util.Date
import me.devyonghee.apressbanking.transaction.TransactionJson

data class Account(
    val id: Long,
    val balance: BigDecimal,
    val lastStatementDate: Date,
    val transactions: List<TransactionJson> = listOf(),
) {
    fun addedTransaction(transaction: TransactionJson): Account {
        return copy(
            transactions = transactions + transaction
        )
    }
}