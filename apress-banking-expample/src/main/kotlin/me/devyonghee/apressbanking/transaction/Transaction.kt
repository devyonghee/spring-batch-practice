package me.devyonghee.apressbanking.transaction

import java.math.BigDecimal
import java.util.Date
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlRootElement(name = "transaction")
@XmlAccessorType(XmlAccessType.FIELD)
data class Transaction(
    var transactionId: Long,
    var accountId: Long,
    var description: String,
    var credit: BigDecimal?,
    var debit: BigDecimal?,
    @XmlJavaTypeAdapter(JaxbDateSerializer::class)
    var timestamp: Date,
) {
    val transactionAmount: BigDecimal
        get() {
            return (credit ?: BigDecimal.ZERO).add(debit ?: BigDecimal.ZERO)
        }
}
