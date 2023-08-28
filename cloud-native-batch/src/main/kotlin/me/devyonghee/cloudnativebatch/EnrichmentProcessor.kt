package me.devyonghee.cloudnativebatch

import org.springframework.batch.item.ItemProcessor
import org.springframework.retry.annotation.CircuitBreaker
import org.springframework.retry.annotation.Recover
import org.springframework.web.client.RestTemplate

class EnrichmentProcessor(
    private val restTemplate: RestTemplate,
) : ItemProcessor<Foo, Foo> {

    @CircuitBreaker(maxAttempts = 1)
    override fun process(item: Foo): Foo {
        return item.copy(
            message = restTemplate.getForEntity(
                "http://localhost:8080/enrich",
                String::class.java
            ).body
        )
    }

    @Recover
    fun fallback(foo: Foo): Foo {
        return foo.copy(message = "error")
    }
}
