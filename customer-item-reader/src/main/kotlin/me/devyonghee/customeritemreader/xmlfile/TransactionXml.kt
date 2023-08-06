package me.devyonghee.customeritemreader.xmlfile

import java.util.Date

data class TransactionXml(
    var accountNumber: String? = null,
    var transactionDate: Date? = null,
    var amount: Double? = null,
)