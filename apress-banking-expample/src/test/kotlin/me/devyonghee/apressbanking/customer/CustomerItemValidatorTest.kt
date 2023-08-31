package me.devyonghee.apressbanking.customer

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.batch.item.validator.ValidationException
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class CustomerItemValidatorTest : StringSpec({

    val jdbcTemplate: NamedParameterJdbcTemplate = mockk<NamedParameterJdbcTemplate>()
    val validator = CustomerItemValidator(jdbcTemplate)

    "사용자 검증" {
        // given
        val customer = CustomerUpdate(5L)
        val parameterSlot: CapturingSlot<Map<String, Long>> = slot<Map<String, Long>>()
        every { jdbcTemplate.queryForObject(any(String::class), capture(parameterSlot), Long::class.java) } returns 2L
        // when
        validator.validate(customer)
        // then
        parameterSlot.captured["id"] shouldBe 5L
    }

    "사용자 검증 실패" {
        // given
        val parameterSlot: CapturingSlot<Map<String, Long>> = slot<Map<String, Long>>()
        every { jdbcTemplate.queryForObject(any(String::class), capture(parameterSlot), Long::class.java) } returns 0L
        // when & then
        shouldThrow<ValidationException> { validator.validate(CustomerUpdate(5L)) }
            .shouldHaveMessage(Regex("^customer id \\d was not able to be found$"))
    }
})
