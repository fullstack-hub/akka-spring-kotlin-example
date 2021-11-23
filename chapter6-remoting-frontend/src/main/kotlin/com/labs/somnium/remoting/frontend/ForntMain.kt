package com.labs.somnium.remoting.frontend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FrontMain

fun main(args: Array<String>) {
    runApplication<FrontMain>(*args)
}
