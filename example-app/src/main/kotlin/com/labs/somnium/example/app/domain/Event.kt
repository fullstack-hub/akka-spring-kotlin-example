package com.labs.somnium.example.app.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("events")
data class Event(
    val name: String,
    val tickets: Int,
    @Id val id: Long? = null,
)
