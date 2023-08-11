package me.devyonghee.customeritemwriter

data class Customer(
    val firstName: String,
    val middleInitial: String,
    val lastName: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
)