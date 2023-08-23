package me.devyonghee.apressbanking.statement

import me.devyonghee.apressbanking.customer.Customer

data class Statement(
    val customer: Customer,
    val accounts: List<Account> = listOf(),
) {
    fun changedAccounts(accounts: List<Account>): Statement {
        return copy(accounts = accounts)
    }
}