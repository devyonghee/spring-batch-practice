package me.devyonghee.customeritemreader.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Date

data class TransactionJson @JsonCreator constructor(
    @JsonProperty("accountNumber") val accountNumber: String,
    @JsonProperty("transactionDate") val transactionDate: Date,
    @JsonProperty("amount") val amount: Double,
)