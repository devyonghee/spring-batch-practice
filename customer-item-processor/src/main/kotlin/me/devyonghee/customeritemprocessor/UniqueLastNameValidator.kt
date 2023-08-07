package me.devyonghee.customeritemprocessor

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamSupport
import org.springframework.batch.item.validator.ValidationException
import org.springframework.batch.item.validator.Validator

class UniqueLastNameValidator : ItemStreamSupport(), Validator<Customer> {

    private var lastNames = mutableSetOf<String>()

    override fun validate(customer: Customer) {
        if (customer.lastName in lastNames) {
            throw ValidationException("Duplicate name was found: ${customer.lastName}")
        }
        lastNames.add(customer.lastName)
    }

    @Suppress("removal")
    override fun open(executionContext: ExecutionContext) {
        val lastNames: String = getExecutionContextKey(LAST_NAMES_KEY)

        if (executionContext.containsKey(lastNames)) {
            @Suppress("UNCHECKED_CAST")
            this.lastNames = (executionContext.get(lastNames) as Collection<String>).toMutableSet()
        }
    }

    @Suppress("removal")
    override fun update(executionContext: ExecutionContext) {
        executionContext.put(getExecutionContextKey(LAST_NAMES_KEY), lastNames.toSet())
    }

    companion object {
        private const val LAST_NAMES_KEY = "lastNames"
    }
}