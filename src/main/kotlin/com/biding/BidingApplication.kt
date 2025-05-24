package com.biding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [FlywayAutoConfiguration::class])
class BidingApplication

fun main(args: Array<String>) {
    runApplication<BidingApplication>(*args)
}
