package me.devyonghee.filecustomertransactionitemreaderjob.flatfile

import java.util.Date

data class Transaction(
    val accountNumber: String,
    val transactionDate: Date,
    val amount: Double,
) : CustomerLineType