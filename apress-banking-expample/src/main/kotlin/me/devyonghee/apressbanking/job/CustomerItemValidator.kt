package me.devyonghee.apressbanking.job

import org.springframework.batch.item.validator.ValidationException
import org.springframework.batch.item.validator.Validator
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class CustomerItemValidator(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : Validator<CustomerUpdate> {

    override fun validate(customer: CustomerUpdate) {
        val count: Long =
            jdbcTemplate.queryForObject(FIND_CUSTOMER, mapOf("id" to customer.customerId), Long::class.java) ?: 0L

        if (count == 0L) {
            throw ValidationException("customer id ${customer.customerId} was not able to be found")
        }
    }

    companion object {
        private const val FIND_CUSTOMER = """
            SELECT COUNT(*) FROM CUSTOMER WHERE customer_id = :id
        """
    }
}