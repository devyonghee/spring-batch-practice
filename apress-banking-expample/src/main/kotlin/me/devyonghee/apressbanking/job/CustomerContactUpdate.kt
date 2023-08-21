package me.devyonghee.apressbanking.job

class CustomerContactUpdate(
    override val customerId: Long,
    emailAddress: String?,
    homePhone: String?,
    cellPhone: String?,
    workPhone: String?,
    val notificationPreference: Int?,
) : CustomerUpdate(customerId) {

    val emailAddress = emailAddress.takeIf { !it.isNullOrBlank() }
    val homePhone = homePhone.takeIf { !it.isNullOrBlank() }
    val cellPhone = cellPhone.takeIf { !it.isNullOrBlank() }
    val workPhone = workPhone.takeIf { !it.isNullOrBlank() }
}