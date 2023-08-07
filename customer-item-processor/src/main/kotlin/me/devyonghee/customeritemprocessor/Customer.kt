package me.devyonghee.customeritemprocessor

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class Customer(

    @field:NotNull(message = "first name is required")
    @field:Pattern(regexp = "[a-zA-Z]+", message = "first name must be alphabetical")
    val firstName: String,

    @field:Size(min = 1, max = 1)
    @field:Pattern(regexp = "[a-zA-Z]", message = "middle initial must be alphabetical")
    val middleInitial: String,

    @field:NotNull(message = "last name is required")
    @field:Pattern(regexp = "[a-zA-Z]+", message = "last name must be alphabetical")
    val lastName: String,

    @field:NotNull(message = "address is required")
    @field:Pattern(regexp = "[0-9a-zA-Z\\. ]+")
    val address: String,

    @field:NotNull(message = "city is required")
    @field:Pattern(regexp = "[0-9a-zA-Z\\. ]+")
    val city: String,

    @field:NotNull(message = "state is required")
    @field:Size(min = 2, max = 2)
    @field:Pattern(regexp = "[A-Z]{2}")
    val state: String,

    @field:NotNull(message = "zip code is required")
    @field:Size(min = 5, max = 5)
    @field:Pattern(regexp = "\\d{5}")
    val zipCode: String,
)