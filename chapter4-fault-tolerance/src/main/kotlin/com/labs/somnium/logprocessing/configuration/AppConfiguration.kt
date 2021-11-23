package com.labs.somnium.logprocessing.configuration

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import com.labs.somnium.logprocessing.LogProcessingSupervisor
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfiguration {
    private val system: ActorSystem<Any>
    private lateinit var supervisor: ActorRef<Any>

    init {
        val sources = listOf("file:///source1", "file:///source2", "file:///source3")
        val databaseUrls = listOf("http://mydatabase1", "http://mydatabase2", "http://mydatabase3")
        val rootBehavior = Behaviors.setup { context: ActorContext<Any> ->
            supervisor = context.spawn(LogProcessingSupervisor.create(sources, databaseUrls), "boxOffice")
            Behaviors.empty()
        }
        system = ActorSystem.create(rootBehavior, "LogProcessingSupervisor")
    }
}
