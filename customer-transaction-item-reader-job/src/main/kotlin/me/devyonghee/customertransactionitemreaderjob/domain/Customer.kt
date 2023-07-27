package me.devyonghee.customertransactionitemreaderjob.domain

data class Customer(
    val firstName: String,
    val middleInitial: String,
    val lastName: String,
    val addressNumber: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
) : CustomerLineType {
    private val transactions: MutableList<Transaction> = mutableListOf()

    fun addTransaction(transaction: Transaction) {
        transactions.add(transaction)
    }

    override fun toString(): String {
        return "Customer(firstName='$firstName', middleInitial='$middleInitial', lastName='$lastName', addressNumber='$addressNumber', street='$street', city='$city', state='$state', zipCode='$zipCode', transactions=$transactions)"
    }
}
