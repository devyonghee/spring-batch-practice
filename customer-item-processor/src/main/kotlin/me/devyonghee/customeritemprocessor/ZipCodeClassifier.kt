package me.devyonghee.customeritemprocessor

import org.springframework.batch.item.ItemProcessor
import org.springframework.classify.Classifier

class ZipCodeClassifier(
    private val oddItemProcessor: ItemProcessor<Customer, Customer>,
    private val evenItemProcessor: ItemProcessor<Customer, Customer>,
) : Classifier<Customer, ItemProcessor<*, out Customer>> {

    override fun classify(classifiable: Customer): ItemProcessor<Customer, Customer> {
        return if (classifiable.zipCode.toInt() % 2 == 0) {
            evenItemProcessor
        } else {
            oddItemProcessor
        }
    }
}