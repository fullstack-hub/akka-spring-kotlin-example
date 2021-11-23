package com.labs.somnium.example.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AppMain

fun main(args: Array<String>) {
    runApplication<AppMain>(*args)
}
