package com.labs.somnium.extensions

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Scheduler
import akka.actor.typed.javadsl.AskPattern
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.future.await
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

private lateinit var extensions: ActorExtensions

@Component
private class ActorExtensions(
    val actorSystem: ActorSystem<Void>,
    val askTimeout: Duration,
) {
    @PostConstruct
    private fun init() {
        extensions = this
    }
}

suspend fun <Request, Response> ActorRef<Request>.ask(
    timeout: Duration = extensions.askTimeout,
    scheduler: Scheduler = extensions.actorSystem.scheduler(),
    f: (ActorRef<Response>) -> Request
): Response {
    return AskPattern.ask(this, f, timeout, scheduler).await()
}

fun <Request, Response> ActorRef<Request>.askAsync(
    timeout: Duration = extensions.askTimeout,
    scheduler: Scheduler = extensions.actorSystem.scheduler(),
    f: (ActorRef<Response>) -> Request
): Deferred<Response> {
    return AskPattern.ask(this, f, timeout, scheduler).asDeferred()
}
