package me.devyonghee.apressbanking.job

class CustomerAddressUpdate(
    override val customerId: Long,
    address1: String?,
    address2: String?,
    city: String?,
    state: String?,
    postalCode: String?,
) : CustomerUpdate(customerId) {
    val address1 = address1.takeIf { !it.isNullOrBlank() }
    val address2 = address2.takeIf { !it.isNullOrBlank() }
    val city = city.takeIf { !it.isNullOrBlank() }
    val state = state.takeIf { !it.isNullOrBlank() }
    val postalCode = postalCode.takeIf { !it.isNullOrBlank() }
}