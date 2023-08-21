package me.devyonghee.apressbanking.job

class CustomerNameUpdate(
    override val customerId: Long,
    firstName: String?,
    middleName: String?,
    lastName: String?,
) : CustomerUpdate(customerId) {

    val firstName = firstName.takeIf { !it.isNullOrBlank() }
    val middleName = middleName.takeIf { !it.isNullOrBlank() }
    val lastName = lastName.takeIf { !it.isNullOrBlank() }
}