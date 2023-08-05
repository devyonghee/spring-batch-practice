package me.devyonghee.customertransactionitemreader.flatfile

import java.util.Date

data class Transaction(
    val accountNumber: String,
    val transactionDate: Date,
    val amount: Double,
) : CustomerLineType