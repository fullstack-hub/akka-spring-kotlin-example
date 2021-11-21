package com.labs.somnium

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

class TicketSeller(
    context: ActorContext<Command>,
    private val event: String
) : AbstractBehavior<TicketSeller.Command>(context) {

    private var tickets = mutableListOf<Ticket>()

    override fun createReceive(): Receive<Command> {
        return newReceiveBuilder()
            .onMessage(Add::class.java, ::onAdd)
            .onMessage(Buy::class.java, ::onBuy)
            .onMessage(GetEvent::class.java, ::onGetEvent)
            .onMessage(Cancel::class.java, ::onCancel)
            .build()
    }

    private fun onAdd(add: Add): Behavior<Command> {
        tickets.addAll(add.tickets)
        return this
    }

    private fun onBuy(buy: Buy): Behavior<Command> {
        val entries = tickets.take(buy.tickets)
        if (entries.size >= buy.tickets) {
            buy.replyTo.tell(Tickets(event, entries))
            tickets = tickets.drop(buy.tickets).toMutableList()
        } else {
            buy.replyTo.tell(Tickets(event))
        }
        return this
    }

    private fun onGetEvent(getEvent: GetEvent): Behavior<Command> {
        getEvent.replyTo.tell(BoxOffice.Event(event, tickets.size))
        return this
    }

    private fun onCancel(cancel: Cancel): Behavior<Command> {
        cancel.replyTo.tell(BoxOffice.Event(event, tickets.size))
        return Behaviors.stopped() // scala: self ! PoisonPill
    }

    companion object {
        fun create(name: String): Behavior<Command> {
            return Behaviors.setup { TicketSeller(it, name) }
        }
    }

    interface Command

    data class Add(val tickets: List<Ticket>) : Command
    data class Buy(val tickets: Int, val replyTo: ActorRef<Command>) : Command
    data class Ticket(val id: Int) : Command
    data class Tickets(val event: String, val entries: List<Ticket> = emptyList()) : Command

    data class GetEvent(val replyTo: ActorRef<BoxOffice.Command>) : Command
    data class Cancel(val replyTo: ActorRef<BoxOffice.Command>) : Command
}
