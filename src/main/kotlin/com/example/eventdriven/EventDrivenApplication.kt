package com.example.eventdriven

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventDrivenApplication

fun main(args: Array<String>) {
	runApplication<EventDrivenApplication>(*args)
}
