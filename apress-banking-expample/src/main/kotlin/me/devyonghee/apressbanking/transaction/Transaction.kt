package me.devyonghee.apressbanking.transaction

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter
import java.math.BigDecimal
import java.util.Date

@XmlRootElement(name = "customer")
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
