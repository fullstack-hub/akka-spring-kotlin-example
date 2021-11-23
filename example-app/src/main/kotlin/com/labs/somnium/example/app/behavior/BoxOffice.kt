@file:Suppress("UNCHECKED_CAST")

package com.labs.somnium.example.app.behavior

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.labs.somnium.akka.typed.coroutine.askAsync
import com.labs.somnium.example.app.domain.Event
import com.labs.somnium.example.app.repository.EventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.stereotype.Service
import java.time.Duration
import javax.annotation.PostConstruct

class BoxOffice(
    context: ActorContext<Command>,
) : AbstractBehavior<BoxOffice.Command>(context) {

    private val children = hashMapOf<String, ActorRef<TicketSeller.Command>>()

    override fun createReceive(): Receive<Command> {
        return newReceiveBuilder()
            .onMessage(CreateEvent::class.java, ::onCreateEvent)
            .onMessage(GetTickets::class.java, ::onGetTickets)
            .onMessage(GetEvent::class.java, ::onGetEvent)
            .onMessage(GetEvents::class.java, ::onGetEvents)
            .onMessage(CancelEvent::class.java, ::onCancelEvent)
            .build()
    }

    private fun createTicketSeller(name: String): ActorRef<TicketSeller.Command> {
        return context.spawn(TicketSeller.create(name), name)
    }

    private fun onCreateEvent(createEvent: CreateEvent): Behavior<Command> {
        fun create() {
            val eventTickets = createTicketSeller(createEvent.name) // create child
            children[createEvent.name] = eventTickets

            val newTickets = (1..createEvent.tickets).map { ticketId ->
                TicketSeller.Ticket(ticketId)
            }.toList()

            eventTickets.tell(TicketSeller.Add(newTickets))
            createEvent.replyTo.tell(EventCreated(Event(createEvent.name, createEvent.tickets)))
        }

        children[createEvent.name]?.let { createEvent.replyTo.tell(EventExists) } ?: create()
        return this
    }

    private fun onGetTickets(getTickets: GetTickets): Behavior<Command> {
        fun notFound() = getTickets.replyTo.tell(TicketSeller.Tickets(getTickets.event))
        fun buy(child: ActorRef<TicketSeller.Command>) =
            child.tell(TicketSeller.Buy(getTickets.tickets, getTickets.replyTo))

        children[getTickets.event]?.let { buy(it) } ?: notFound()
        return this
    }

    private fun onGetEvent(getEvent: GetEvent): Behavior<Command> {
        fun notFound() = getEvent.replyTo.tell(NotFoundEvent)
        fun getEvent(child: ActorRef<TicketSeller.Command>) = child.tell(TicketSeller.GetEvent(getEvent.replyTo))
        children[getEvent.name]?.let { getEvent(it) } ?: notFound()
        return this
    }

    private fun onGetEvents(getEvents: GetEvents): Behavior<Command> {
        val events = children.map { entry ->
            val child = entry.value
            val event: Deferred<Command> =
                child.askAsync(Duration.ofSeconds(3), context.system.scheduler()) {
                    TicketSeller.GetEvent(it)
                }
            event as Deferred<Event>
        }
        getEvents.replyTo.tell(Events(events))
        return this
    }

    private fun onCancelEvent(cancelEvent: CancelEvent): Behavior<Command> {
        fun notFound() = cancelEvent.replyTo.tell(NotFoundEvent)
        fun cancelEvent(child: ActorRef<TicketSeller.Command>) = child.tell(TicketSeller.Cancel(cancelEvent.replyTo))
        children[cancelEvent.name]?.let { cancelEvent(it) } ?: notFound()
        return this
    }

    @Service
    class BoxOfficeService(private val eventRepository: EventRepository) {
        @PostConstruct
        fun init() {
            service = this
        }

        suspend fun save(event: com.labs.somnium.example.app.domain.Event): com.labs.somnium.example.app.domain.Event {
            return eventRepository.save(event)
        }

        fun saveAsync(event: com.labs.somnium.example.app.domain.Event): Deferred<com.labs.somnium.example.app.domain.Event> {
            return CoroutineScope(Dispatchers.IO).async {
                return@async save(event)
            }
        }
    }

    companion object {
        lateinit var service: BoxOfficeService
        fun create(): Behavior<Command> {
            return Behaviors.setup(::BoxOffice)
        }
    }

    interface Command

    data class CreateEvent(val name: String, val tickets: Int, val replyTo: ActorRef<EventResponse>) : Command
    data class GetEvent(val name: String, val replyTo: ActorRef<Command>) : Command
    data class GetEvents(val replyTo: ActorRef<Command>) : Command

    data class GetTickets(val event: String, val tickets: Int, val replyTo: ActorRef<TicketSeller.Command>) : Command
    data class CancelEvent(val name: String, val replyTo: ActorRef<Command>) : Command

    data class Event(val name: String, val tickets: Int) : Command
    data class Events(val events: List<Deferred<Event>>) : Command
    object NotFoundEvent : Command

    sealed interface EventResponse
    data class EventCreated(val event: Event) : EventResponse
    object EventExists : EventResponse
}
