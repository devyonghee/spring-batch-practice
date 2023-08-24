package me.devyonghee.apressbanking.customer

data class Customer(
    val id: Long,
    val firstName: String,
    val middleName: String?,
    val lastName: String,
    val address1: String,
    val address2: String?,
    val city: String,
    val state: String,
    val postalCode: String,
    val ssn: String,
    val emailAddress: String?,
    val homePhone: String?,
    val cellPhone: String?,
    val workPhone: String?,
    val notificationPreference: Int,
)