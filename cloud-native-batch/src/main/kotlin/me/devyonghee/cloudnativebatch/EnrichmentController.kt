package me.devyonghee.cloudnativebatch

import kotlin.random.Random
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EnrichmentController {

    private var count: Int = 0;

    @GetMapping("/enrich")
    fun enrich(): String {
        if (Random.Default.nextBoolean()) {
            throw RuntimeException("crewed up")
        }
        return "Enriched ${++count}"
    }
}