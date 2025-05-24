package com.biding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication(exclude = [FlywayAutoConfiguration::class])
@ComponentScan(basePackages = ["com.biding"])
class BidingApplication

fun main(args: Array<String>) {
    runApplication<BidingApplication>(*args)
}
