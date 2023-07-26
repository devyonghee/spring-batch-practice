package me.devyonghee.flatfilecustomercopyjob

import java.util.Date

sealed interface CustomerLineType

data class Customer(
    val firstName: String,
    val middleInitial: String,
    val lastName: String,
    val addressNumber: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
) : CustomerLineType

data class Transaction(
    val accountNumber: String,
    val transactionDate: Date,
    val amount: Double,
) : CustomerLineType