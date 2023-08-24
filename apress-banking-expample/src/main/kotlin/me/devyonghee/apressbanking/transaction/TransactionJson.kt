package me.devyonghee.apressbanking.transaction

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.Date

data class TransactionJson @JsonCreator constructor(
    @JsonProperty("transactionId")
    val transactionId: Long,
    @JsonProperty("accountId")
    val accountId: Long,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("credit")
    val credit: BigDecimal?,
    @JsonProperty("debit")
    val debit: BigDecimal?,
    @JsonProperty("timestamp")
    val timestamp: Date,
) {
    val transactionAmount: BigDecimal
        get() {
            return (credit ?: BigDecimal.ZERO).add(debit ?: BigDecimal.ZERO)
        }
}