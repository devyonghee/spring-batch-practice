package me.devyonghee.customeritemprocessor

import java.util.Locale
import org.springframework.stereotype.Service

@Service
class UpperCaseNameService() {

    fun upperCase(customer: Customer): Customer {
        return customer.copy(
            firstName = customer.firstName.uppercase(Locale.getDefault()),
            middleInitial = customer.middleInitial.uppercase(Locale.getDefault()),
            lastName = customer.lastName.uppercase(Locale.getDefault())
        )
    }
}