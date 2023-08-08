package me.devyonghee.customeritemprocessor

import java.util.Locale
import org.springframework.stereotype.Service

@Service
class FirstNameUpperCaseNameService() {

    fun firstNameUpperCase(customer: Customer): Customer {
        return customer.copy(
            firstName = customer.firstName.uppercase(Locale.getDefault()),
        )
    }
}