package me.devyonghee.customertransactionitemreader.flatfile

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
}
