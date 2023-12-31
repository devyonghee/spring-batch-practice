package me.devyonghee.customeritemreader.jpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<Customer, Long> {

    fun findByCity(city: String, pageable: Pageable): Page<Customer>
}