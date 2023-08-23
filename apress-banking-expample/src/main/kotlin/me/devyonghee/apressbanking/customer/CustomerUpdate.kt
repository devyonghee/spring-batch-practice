package me.devyonghee.apressbanking.customer

sealed class CustomerUpdate(
    open val customerId: Long,
)