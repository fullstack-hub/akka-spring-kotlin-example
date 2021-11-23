package com.labs.somnium.logprocessing

import akka.actor.testkit.typed.javadsl.ActorTestKit
import org.junit.jupiter.api.Test

class LogProcessingTest {
    private val testKit: ActorTestKit = ActorTestKit.create()

    @Test
    fun test() {
        val sources = listOf("file:///source1", "file:///source2", "file:///source3")
        val databaseUrls = listOf("http://mydatabase1", "http://mydatabase2", "http://mydatabase3")
        val supervisor = testKit.spawn(LogProcessingSupervisor.create(sources, databaseUrls))
        Thread.sleep(3000)
    }
}
