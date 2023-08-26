package me.devyonghee.optimizationbatch

import java.math.BigDecimal
import java.util.Date

data class Transaction(
    val account: String,
    val amount: BigDecimal,
    val timestamp: Date,
)
