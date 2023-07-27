package me.devyonghee.customertransactionitemreaderjob.domain

import java.util.Date

data class Transaction(
    val accountNumber: String,
    val transactionDate: Date,
    val amount: Double,
) : CustomerLineType