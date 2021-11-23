package com.labs.somnium.remoting.frontend

import akka.actor.ActorIdentity
import akka.actor.ReceiveTimeout
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import java.time.Duration

class RemoteLookupProxy(context: ActorContext<Any>, private val path: String) : AbstractBehavior<Any>(context) {
    init {
        context.setReceiveTimeout(Duration.ofSeconds(3), ReceiveTimeout::class.java)
        sendIdentifyRequest()
    }

    private fun sendIdentifyRequest() {
        val selection = context.classicActorContext().actorSelection(path)
        //        selection.tell(Identify(path), ActorRef.noSender())
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
            .onMessage(ReceiveTimeout::class.java, ::onReceiveTimeout)
            .build()
    }

    private fun onActorIdentity(actorIdentity: ActorIdentity): Behavior<Any> {
        return this
    }

    private fun onReceiveTimeout(receiveTimeout: ReceiveTimeout): Behavior<Any> {
        println("timeout")
        sendIdentifyRequest()
        return this
    }

    companion object {
        fun create(path: String): Behavior<Any> {
            return Behaviors.setup { RemoteLookupProxy(it, path) }
        }
    }
}
