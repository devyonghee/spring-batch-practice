package me.devyonghee.cloudnativebatch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class CloudNativeBatchApplication

fun main(args: Array<String>) {
    runApplication<CloudNativeBatchApplication>(*args)
}