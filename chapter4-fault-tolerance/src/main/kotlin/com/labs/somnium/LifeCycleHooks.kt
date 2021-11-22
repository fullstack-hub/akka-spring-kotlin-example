package com.labs.somnium

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.PreRestart
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

// Akka Typed에서는 PreStart, PostRestart가 없다. 생성자에 배치하거나 지연된 동작에 배치하여 대치된다.
class LifeCycleHooks(context: ActorContext<Any>) : AbstractBehavior<Any>(context) {

    init {
        println("Constructor")
    }

    private fun onPreRestart(signal: PreRestart): Behavior<Any> {
        println("preRestart")
        return this
    }

    private fun onPostStop(signal: PostStop): Behavior<Any> {
        println("postStop")
        return this
    }

    private fun onRestart(): Behavior<Any> {
        throw IllegalStateException("force restart")
    }

    private fun onSampleMessage(sampleMessage: SampleMessage): Behavior<Any> {
        println("Receive")
        sampleMessage.replyTo.tell(sampleMessage.message)
        return this
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
            .onMessageEquals("restart", ::onRestart)
            .onMessage(SampleMessage::class.java, ::onSampleMessage)
            .onSignal(PreRestart::class.java, ::onPreRestart)
            .onSignal(PostStop::class.java, ::onPostStop)
            .build()
    }

    companion object {
        fun create(): Behavior<Any> = Behaviors.setup(::LifeCycleHooks)
    }

    data class SampleMessage(val message: String, val replyTo: ActorRef<String>)
}
