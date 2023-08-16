package me.devyonghee.customeritemwriter.service

import me.devyonghee.customeritemwriter.flatfile.Customer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CustomerService {

    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun logCustomer(customer: Customer) {
        log.info("i just saved {}", customer)
    }

    fun logCustomerAddress(address: String, city: String, state: String, zipCode: String) {
        log.info("i just saved an address: {}, {}, {}, {}", address, city, state, zipCode)
    }
}