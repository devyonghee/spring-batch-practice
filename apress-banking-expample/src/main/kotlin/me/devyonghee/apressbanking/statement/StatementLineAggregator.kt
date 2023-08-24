package me.devyonghee.apressbanking.statement

import java.util.Date
import me.devyonghee.apressbanking.customer.Customer
import me.devyonghee.apressbanking.transaction.Transaction
import me.devyonghee.apressbanking.transaction.TransactionJson
import org.springframework.batch.item.file.transform.LineAggregator

object StatementLineAggregator : LineAggregator<Statement> {

    private val ADDRESS_LINE_ONE: String = "Apress Banking".padStart(121)
    private val ADDRESS_LINE_TWO: String = "1060 West Addison St.".padStart(120)
    private val ADDRESS_LINE_THREE: String = "Chicago, IL 60613".padStart(120)
    private val STATEMENT_DATE_LINE: String = "Your Account Summary ${"Statement Period".padStart(78)}%tD to %tD\n\n"

    override fun aggregate(statement: Statement): String {
        return """
            ${formatHeader(statement)}
            
            ${formatAccount(statement)}
        """.trimIndent()
    }

    private fun formatAccount(statement: Statement) =
        statement.accounts.joinToString("\n") { account ->
            val transactions: List<TransactionJson> = account.transactions

            """
                ${STATEMENT_DATE_LINE.format(account.lastStatementDate, Date())}
                ${
                transactions.joinToString("\n") { transaction ->
                    " %tD %-50s %8.2f".format(
                        transaction.timestamp,
                        transaction.description,
                        transaction.transactionAmount
                    )
                }
            }
                ${"Total Debit:".padStart(80)} ${"%14.2f".format(transactions.mapNotNull { it.debit }.sumOf { it })}
                ${"Total Credit:".padStart(81)} ${"%13.2f".format(transactions.mapNotNull { it.credit }.sumOf { it })}
                ${"Balance:".padStart(76)} ${"%18.2f".format(account.balance)}
                """.trimIndent()
        }

    private fun formatHeader(statement: Statement): String {
        val customer: Customer = statement.customer
        val customerName = "${customer.firstName} ${customer.lastName}"
        val address = "${customer.city}, ${customer.state} ${customer.postalCode}"

        return """
           $customerName${ADDRESS_LINE_ONE.substring(customerName.length)}
           ${customer.address1}${ADDRESS_LINE_TWO.substring(customer.address1.length)}
           $address${ADDRESS_LINE_THREE.substring(address.length)}
        """.trimIndent()
    }
}