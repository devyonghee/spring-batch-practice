package me.devyonghee.customertransactionitemreader.custom

import kotlin.random.Random
import me.devyonghee.customertransactionitemreader.jdbc.Customer
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.support.AbstractItemStreamItemReader

class CustomerItemReader : AbstractItemStreamItemReader<Customer>() {

    private var currentIndex = 0

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

    override fun read(): Customer? {
        if (currentIndex == 50) {
            throw RuntimeException("this will end you execution")
        }
        return customers.getOrNull(currentIndex++)
    }

    // 중간에 에러가 발생하면 건너 뛰고 실행하도록 설정
    override fun open(executionContext: ExecutionContext) {
        if (!executionContext.containsKey(getExecutionContextKey(INDEX_KEY))) {
            currentIndex = 0
            return
        }
        currentIndex = executionContext.getInt(getExecutionContextKey(INDEX_KEY))
            .takeIf { it != 50 } ?: 51
    }

    override fun update(executionContext: ExecutionContext) {
        executionContext.putInt(getExecutionContextKey(INDEX_KEY), currentIndex)
    }

    companion object {
        private val FIRST_NAMES: List<String> = listOf(
            "Michael", "Warren", "Ann", "Terrence",
            "Erica", "Laura", "Steve", "Larry"
        )
        private const val MIDDLE_INITIAL: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val LAST_NAMES: List<String> =
            listOf("Gates", "Darrow", "Donnelly", "Jobs", "Buffett", "Ellison", "Obama")
        private val STREETS: List<String> = listOf(
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
        private val CITIES: List<String> = listOf("Chicago", "New York", "Hollywood", "Aurora", "Omaha", "Atherton")
        private val STATES: List<String> = listOf("IL", "NY", "CA", "NE")
        private const val INDEX_KEY: String = "current.index.customers"
    }
}