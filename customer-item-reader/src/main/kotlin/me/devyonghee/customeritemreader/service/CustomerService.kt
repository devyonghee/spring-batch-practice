package me.devyonghee.customeritemreader.service

import kotlin.random.Random
import me.devyonghee.customeritemreader.jdbc.Customer
import org.springframework.stereotype.Service

@Service
class CustomerService {
    private var currentIndex: Int = 0
    private val customers: List<Customer> = (1..100).map { i ->
        Customer(
            id = i.toLong(),
            firstName = FIRST_NAMES.random(),
            middleInitial = MIDDLE_INITIAL.random().toString(),
            lastName = LAST_NAMES.random(),
            address = STREETS.random(),
            city = CITIES.random(),
            state = STATES.random(),
            zipCode = Random.nextInt(99999).toString()
        )
    }

    fun getCustomers(): Customer? {
        return customers.getOrNull(currentIndex++)
    }


    companion object {
        private val FIRST_NAMES = listOf(
            "Michael", "Warren", "Ann", "Terrence",
            "Erica", "Laura", "Steve", "Larry"
        )
        private const val MIDDLE_INITIAL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val LAST_NAMES = listOf("Gates", "Darrow", "Donnelly", "Jobs", "Buffett", "Ellison", "Obama")
        private val STREETS = listOf(
            "4th Street",
            "Wall Street",
            "Fifth Avenue",
            "Mt. Lee Drive",
            "Jeopardy Lane",
            "Infinite Loop Drive",
            "Farnam Street",
            "Isabella Ave",
            "S. Greenwood Ave"
        )
        private val CITIES = listOf("Chicago", "New York", "Hollywood", "Aurora", "Omaha", "Atherton")
        private val STATES = listOf("IL", "NY", "CA", "NE")
    }
}