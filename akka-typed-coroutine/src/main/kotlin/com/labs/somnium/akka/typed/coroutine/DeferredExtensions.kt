package com.labs.somnium.akka.typed.coroutine

import kotlinx.coroutines.Deferred
import java.util.concurrent.TimeoutException

suspend fun <T> Collection<Deferred<T>>.awaitAllNotTimeout(): List<T> {
    return this.mapNotNull {
        try {
            it.await()
        } catch (e: TimeoutException) {
            null
        }
    }
}
