package com.labs.somnium

import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.javadsl.Behaviors
import org.junit.jupiter.api.Test

class LifeCycleHooksTest {
    private val testKit: ActorTestKit = ActorTestKit.create()

    @Test
    fun lifeCycle() {
        val testActorRef = testKit.spawn(
            Behaviors.supervise(LifeCycleHooks.create())
                .onFailure(IllegalStateException::class.java, SupervisorStrategy.restart()),
            "LifeCycleHooks"
        )
        val probe = testKit.createTestProbe<String>()
        testActorRef.tell("restart")
        testActorRef.tell(LifeCycleHooks.SampleMessage("msg", probe.ref()))
        probe.expectMessage("msg")
        testKit.stop(testActorRef)
        Thread.sleep(1000)
    }
}
