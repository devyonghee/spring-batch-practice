package me.devyonghee.customeritemprocessor

import org.springframework.batch.item.ItemProcessor

object EventFilteringItemProcessor : ItemProcessor<Customer, Customer> {

    override fun process(item: Customer): Customer? {
        return (item.zipCode.toInt() % 5).takeIf { it != 0 }?.let { item }
    }
}