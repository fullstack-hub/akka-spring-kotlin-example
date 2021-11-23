package com.labs.somnium.logprocessing

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class LogProcessingSupervisor(
    context: ActorContext<Any>,
    sources: List<String>,
    private val databaseUrls: List<String>
) : AbstractBehavior<Any>(context) {

    private var fileWatchers = mutableListOf<ActorRef<FileWatcherProtocol>>()

    init {
        sources.forEach {
            val fileWatcher = context.spawnAnonymous(
                Behaviors.supervise(FileWatcher.create(it, databaseUrls))
                    .onFailure(DiskError::class.java, SupervisorStrategy.stop())
            )
            context.watch(fileWatcher)
            fileWatchers.add(fileWatcher)
        }
    }

    override fun createReceive(): Receive<Any> {
        return newReceiveBuilder()
            .onSignal(Terminated::class.java, ::onTerminated)
            .build()
    }

    private fun onTerminated(signal: Terminated): Behavior<Any> {
        fileWatchers = fileWatchers.filterNot { it == signal.ref() }.toMutableList()
        if (fileWatchers.isEmpty()) {
            context.log.info("Shutting down, all watchers hav failed.")
            context.system.terminate()
        }
        return this
    }

    companion object {
        fun create(sources: List<String>, databaseUrls: List<String>): Behavior<Any> =
            Behaviors.setup {
                LogProcessingSupervisor(it, sources, databaseUrls)
            }
    }
}
