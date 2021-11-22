package com.labs.somnium.configuration

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.stream.alpakka.spring.web.AkkaStreamsRegistrar
import akka.stream.alpakka.spring.web.SpringWebAkkaStreamsProperties
import akka.stream.javadsl.Source
import com.labs.somnium.BoxOffice
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.ReactiveAdapterRegistry
import java.time.Duration

@Configuration
@ConditionalOnClass(Source::class)
@EnableConfigurationProperties(SpringWebAkkaStreamsProperties::class)
class SpringWebAkkaStreamsConfiguration(
    val properties: SpringWebAkkaStreamsProperties
) {
    private val system: ActorSystem<Any>
    private lateinit var boxOffice: ActorRef<BoxOffice.Command>

    init {
        val registry = ReactiveAdapterRegistry.getSharedInstance()
        val rootBehavior = Behaviors.setup { context: ActorContext<Any> ->
            boxOffice = context.spawn(BoxOffice.create(), "boxOffice")
            Behaviors.empty()
        }
        system = ActorSystem.create(rootBehavior, DEFAULT_ACTORY_SYSTEM_NAME)
        AkkaStreamsRegistrar(system).registerAdapters(registry)
    }

    @Bean
    fun askTimeout(): Duration = Duration.ofSeconds(300)

    @Bean
    fun actorSystem() = system

    @Bean
    fun boxOfficeActorRef() = boxOffice

    companion object {
        private const val DEFAULT_ACTORY_SYSTEM_NAME = "SpringWebAkkaStreamsSystem"
    }
}
