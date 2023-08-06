package me.devyonghee.customeritemreader.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "customer")
class Customer(
    @Id
    val id: Long,
    @Column(name = "first_name")
    val firstName: String,
    @Column(name = "middle_initial")
    val middleInitial: String,
    @Column(name = "last_name")
    val lastName: String,
    @Column(name = "address")
    val address: String,
    @Column(name = "city")
    val city: String,
    @Column(name = "state")
    val state: String,
    @Column(name = "zip_code")
    val zipCode: String,
) {

    override fun toString(): String {
        return "Customer(id=$id, firstName='$firstName', middleInitial='$middleInitial', lastName='$lastName', address='$address', city='$city', state='$state', zipCode='$zipCode')"
    }
}