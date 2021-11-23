package com.labs.somnium.logprocessing

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class FileWatcher(
    context: ActorContext<FileWatcherProtocol>,
    source: String,
    databaseUrls: List<String>
) : AbstractBehavior<FileWatcherProtocol>(context) {

    private val logProcessor: ActorRef<LogProcessingProtocol>

    init {
        logProcessor = context.spawn(
            Behaviors.supervise(LogProcessor.create(databaseUrls))
                .onFailure(CorruptedFileException::class.java, SupervisorStrategy.resume()),
            LogProcessor.name
        )
        context.watch(logProcessor)
    }

    override fun createReceive(): Receive<FileWatcherProtocol> {
        return newReceiveBuilder()
            .onMessage(NewFile::class.java, ::onNewFile)
            .onMessage(SourceAbandoned::class.java, ::onSourceAbandoned)
            .onSignal(Terminated::class.java, ::onTerminated)
            .build()
    }

    private fun onNewFile(newFile: NewFile): Behavior<FileWatcherProtocol> {
        logProcessor.tell(LogFile(newFile.file))
        return this
    }

    private fun onSourceAbandoned(sourceAbandoned: SourceAbandoned): Behavior<FileWatcherProtocol> {
        context.log.info("${sourceAbandoned.uri} abandoned, stopping file watcher.")
        return Behaviors.stopped()
    }

    private fun onTerminated(signal: Terminated): Behavior<FileWatcherProtocol> {
        context.log.info("Log processor terminated, stopping file watcher.")
        return Behaviors.stopped()
    }

    companion object {
        fun create(source: String, databaseUrls: List<String>): Behavior<FileWatcherProtocol> {
            return Behaviors.setup { FileWatcher(it, source, databaseUrls) }
        }
    }
}
