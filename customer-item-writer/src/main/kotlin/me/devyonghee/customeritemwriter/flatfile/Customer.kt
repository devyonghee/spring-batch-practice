package me.devyonghee.customeritemwriter.flatfile

data class Customer(
    val firstName: String,
    val middleInitial: String,
    val lastName: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val email: String,
)