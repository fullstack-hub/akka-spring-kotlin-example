package com.labs.somnium.logprocessing

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import java.io.File
import java.util.*

class LogProcessor(
    context: ActorContext<LogProcessingProtocol>,
    private val databaseUrls: Queue<String>
) : AbstractBehavior<LogProcessingProtocol>(context) {

    private var dbWriter: ActorRef<LogProcessingProtocol>

    init {
        val initialDatabaseUrl = databaseUrls.poll()
        dbWriter = createDbWriterAndWatch(initialDatabaseUrl)
    }

    private fun createDbWriterAndWatch(databaseUrl: String): ActorRef<LogProcessingProtocol> {
        val behavior = context.spawn(
            Behaviors.supervise(
                Behaviors.supervise(
                    DbWriter.create(databaseUrl)
                ).onFailure(DbBrokenConnectionException::class.java, SupervisorStrategy.restart())
            ).onFailure(DbNodeDownException::class.java, SupervisorStrategy.stop()),
            DbWriter.name(databaseUrl)
        )
        context.watch(behavior)
        return behavior
    }

    override fun createReceive(): Receive<LogProcessingProtocol> {
        return newReceiveBuilder()
            .onMessage(LogFile::class.java, ::onLogFile)
            .onSignal(Terminated::class.java, ::onTerminated)
            .build()
    }

    private fun onLogFile(logFile: LogFile): Behavior<LogProcessingProtocol> {
        val lines = parse(logFile.file)
        lines.forEach {
            dbWriter.tell(it) // dbWriter에 파일의 각 줄을 보낸다
        }
        return this
    }

    private fun onTerminated(signal: Terminated): Behavior<LogProcessingProtocol> {
        return if (databaseUrls.isNotEmpty()) {
            val newDatabaseUrl = databaseUrls.poll()
            dbWriter = createDbWriterAndWatch(newDatabaseUrl)
            Behaviors.same()
        } else {
            context.log.error("All Db nodes broken, stopping.")
            Behaviors.stopped()
        }
    }

    private fun parse(file: File): List<Line> {
        return emptyList()
    }

    companion object {
        val name = "log_processor_${UUID.randomUUID()}"
        fun create(databaseUrls: List<String>): Behavior<LogProcessingProtocol> {
            return Behaviors.setup { LogProcessor(it, LinkedList(databaseUrls)) }
        }
    }
}
