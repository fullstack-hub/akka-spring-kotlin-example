package com.labs.somnium.remoting.frontend.config

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.stream.alpakka.spring.web.AkkaStreamsRegistrar
import akka.stream.alpakka.spring.web.SpringWebAkkaStreamsProperties
import akka.stream.javadsl.Source
import com.labs.somnium.remoting.frontend.RemoteLookupProxy
import com.typesafe.config.ConfigFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ReactiveAdapterRegistry

@Configuration
@ConditionalOnClass(Source::class)
@EnableConfigurationProperties(SpringWebAkkaStreamsProperties::class)
class FrontendConfiguration(val properties: SpringWebAkkaStreamsProperties) {
    private val system: ActorSystem<Any>
    private val config = ConfigFactory.load("frontend")
    private lateinit var remoteLookupProxy: ActorRef<Any>

    init {
        val registry = ReactiveAdapterRegistry.getSharedInstance()
        val rootBehavior = Behaviors.setup { context: ActorContext<Any> ->
            remoteLookupProxy = context.spawn(RemoteLookupProxy.create(createPath()), "lookupBoxOffice")
            Behaviors.empty()
        }

        system = ActorSystem.create(rootBehavior, "frontend", config)
        AkkaStreamsRegistrar(system).registerAdapters(registry)
    }

    @Bean
    fun actorSystem() = system

    @Bean
    fun boxOfficeActorRef() = remoteLookupProxy

    private fun createPath(): String {
        val backend = config.getConfig("backend")
        val host = backend.getString("host")
        val port = backend.getInt("port")
        val protocol = backend.getString("protocol")
        val systemName = backend.getString("system")
        val actorName = backend.getString("actor")
        return "$protocol://$systemName@$host:$port/$actorName"
    }
}
