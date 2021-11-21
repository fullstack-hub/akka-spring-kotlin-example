package com.labs.somnium

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import com.labs.somnium.extensions.ask
import com.labs.somnium.extensions.awaitAllNotTimeout
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

data class TicketRequest(val tickets: Int)
data class Error(val message: String)

@RestController
@RequestMapping("/events")
class BoxOfficeController(
    private val system: ActorSystem<Void>,
    private val boxOfficeActorRef: ActorRef<BoxOffice.Command>
) {
    @PostMapping("/{name}")
    suspend fun createEvent(
        @PathVariable name: String,
        @RequestBody request: TicketRequest
    ): ResponseEntity<*> {
        val response: BoxOffice.EventResponse = boxOfficeActorRef.ask {
            return@ask BoxOffice.CreateEvent(name, request.tickets, it)
        }
        return when (response) {
            is BoxOffice.EventCreated -> ResponseEntity(BoxOffice.Event(name, request.tickets), HttpStatus.CREATED)
            is BoxOffice.EventExists -> ResponseEntity(Error("$name event exists already."), HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping
    suspend fun getAllEvents(): ResponseEntity<*> {
        val response: BoxOffice.Command = boxOfficeActorRef.ask {
            return@ask BoxOffice.GetEvents(it)
        }

        val events = (response as BoxOffice.Events).events.awaitAllNotTimeout()
        return ResponseEntity(events, HttpStatus.OK)
    }

    @GetMapping("/{name}")
    suspend fun getEvent(@PathVariable name: String): ResponseEntity<BoxOffice.Event> {
        val response: BoxOffice.Command = boxOfficeActorRef.ask {
            return@ask BoxOffice.GetEvent(name, it)
        }
        return when (response) {
            is BoxOffice.Event -> ResponseEntity(response, HttpStatus.OK)
            else -> ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @PostMapping("/{name}/tickets")
    suspend fun buyTicket(
        @PathVariable name: String,
        @RequestBody request: TicketRequest
    ): ResponseEntity<TicketSeller.Tickets> {
        val response: TicketSeller.Command = boxOfficeActorRef.ask {
            return@ask BoxOffice.GetTickets(name, request.tickets, it)
        }
        return when {
            response is TicketSeller.Tickets && response.entries.isNotEmpty() -> ResponseEntity(
                response,
                HttpStatus.CREATED
            )
            else -> ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping("/{name}")
    suspend fun cancelEvent(@PathVariable name: String): ResponseEntity<BoxOffice.Event> {
        val response: BoxOffice.Command = boxOfficeActorRef.ask {
            return@ask BoxOffice.CancelEvent(name, it)
        }
        return when (response) {
            is BoxOffice.Event -> ResponseEntity(response, HttpStatus.OK)
            else -> ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}
