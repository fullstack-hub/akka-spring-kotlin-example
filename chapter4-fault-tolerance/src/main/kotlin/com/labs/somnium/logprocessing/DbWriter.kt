package com.labs.somnium.logprocessing

import akka.actor.typed.Behavior
import akka.actor.typed.PostStop
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class DbWriter(context: ActorContext<LogProcessingProtocol>, databaseUrl: String) :
    AbstractBehavior<LogProcessingProtocol>(context) {
    private val connection = DbCon(databaseUrl)

    override fun createReceive(): Receive<LogProcessingProtocol> {
        return newReceiveBuilder()
            .onMessage(Line::class.java, ::onLine)
            .onSignal(PostStop::class.java, ::onPostStop)
            .build()
    }

    private fun onPostStop(signal: PostStop): Behavior<LogProcessingProtocol> {
        connection.close()
        return this
    }

    private fun onLine(line: Line): Behavior<LogProcessingProtocol> {
        connection.write(mapOf("time" to line.time, "message" to line.message, "messageType" to line.messageType))
        return this
    }

    companion object {
        fun create(databaseUrl: String): Behavior<LogProcessingProtocol> {
            return Behaviors.setup { DbWriter(it, databaseUrl) }
        }

        fun name(databaseUrl: String) = "db-writer-${databaseUrl.split("/").last()}"
    }
}
