import akka.actor.testkit.typed.javadsl.ActorTestKit
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import kotlin.test.AfterTest
import kotlin.test.Test

class AsynchronousTest {
    private val testKit: ActorTestKit = ActorTestKit.create()

    @Test
    fun `ping pong`() {
        val pinger = testKit.spawn(Echo.create())
        val probe = testKit.createTestProbe<Echo.Pong>()
        pinger.tell(Echo.Ping("hello", probe.ref()))
        probe.expectMessage(Echo.Pong("hello"))
    }

    @AfterTest
    fun clenup() {
        testKit.shutdownTestKit()
    }
}

class Echo(context: ActorContext<Ping>) : AbstractBehavior<Echo.Ping>(context) {
    override fun createReceive(): Receive<Ping> {
        return newReceiveBuilder()
            .onMessage(Ping::class.java, ::onPing)
            .build()
    }

    private fun onPing(ping: Ping): Behavior<Ping> {
        ping.replyTo.tell(Pong(ping.message))
        return this
    }

    companion object {
        fun create(): Behavior<Ping> = Behaviors.setup(::Echo)
    }

    data class Ping(val message: String, val replyTo: ActorRef<Pong>)
    data class Pong(val message: String)
}
