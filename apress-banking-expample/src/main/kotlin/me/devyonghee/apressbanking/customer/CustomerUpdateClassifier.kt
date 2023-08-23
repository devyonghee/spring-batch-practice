package me.devyonghee.apressbanking.customer

import org.springframework.batch.item.ItemWriter
import org.springframework.classify.Classifier

class CustomerUpdateClassifier(
    private val recordType1ItemWriter: ItemWriter<CustomerUpdate>,
    private val recordType2ItemWriter: ItemWriter<CustomerUpdate>,
    private val recordType3ItemWriter: ItemWriter<CustomerUpdate>,
) : Classifier<CustomerUpdate, ItemWriter<in CustomerUpdate>> {

    override fun classify(classifiable: CustomerUpdate?): ItemWriter<in CustomerUpdate> {
        return when (classifiable) {
            is CustomerNameUpdate -> recordType1ItemWriter
            is CustomerAddressUpdate -> recordType2ItemWriter
            is CustomerContactUpdate -> recordType3ItemWriter
            else -> throw IllegalArgumentException("invalid type: ${classifiable?.javaClass?.name}")
        }
    }
}