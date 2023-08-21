package me.devyonghee.apressbanking.job

sealed class CustomerUpdate(
    open val customerId: Long,
)