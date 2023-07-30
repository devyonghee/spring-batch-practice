package me.devyonghee.filecustomertransactionitemreaderjob.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class CustomerJson @JsonCreator constructor(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("middleInitial") val middleInitial: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("address") val address: String,
    @JsonProperty("city") val city: String,
    @JsonProperty("state") val state: String,
    @JsonProperty("zipCode") val zipCode: String,
    @JsonProperty("transactions") val transactions: List<TransactionJson>,
)
