package me.devyonghee.apressbanking.statement

import java.math.BigDecimal
import java.util.Date
import me.devyonghee.apressbanking.transaction.Transaction

data class Account(
    val id: Long,
    val balance: BigDecimal,
    val lastStatementDate: Date,
    val transactions: List<Transaction> = listOf(),
) {
    fun addedTransaction(transaction: Transaction): Account {
        return copy(
            transactions = transactions + transaction
        )
    }
}