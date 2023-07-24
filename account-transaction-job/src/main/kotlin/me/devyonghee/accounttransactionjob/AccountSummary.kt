package me.devyonghee.accounttransactionjob

data class AccountSummary(
    val id: Int,
    val accountNumber: String,
    var currentBalance: Double,
)