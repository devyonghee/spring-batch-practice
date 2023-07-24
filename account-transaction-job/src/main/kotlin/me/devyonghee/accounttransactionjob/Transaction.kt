package me.devyonghee.accounttransactionjob

import java.util.Date

data class Transaction(
    val accountNumber: String,
    val timestamp: Date,
    val amount: Double,
) {
}