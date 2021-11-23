package com.labs.somnium.example.app.repository

import com.labs.somnium.example.app.domain.Event
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface EventRepository : CoroutineCrudRepository<Event, Long>
