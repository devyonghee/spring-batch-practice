package me.devyonghee.customeritemreader.jpa

import jakarta.persistence.Query
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider

class CustomerByCityQueryProvider(
    private val cityName: String,
) : AbstractJpaQueryProvider() {

    override fun createQuery(): Query {
        return entityManager.createQuery(
            """
            SELECT c
            FROM Customer c
            WHERE c.city = :city
            """
        ).apply {
            setParameter("city", cityName)
        }
    }

    override fun afterPropertiesSet() {
        require(cityName.isNotBlank()) { "cityName must not be blank" }
    }
}