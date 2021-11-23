package com.labs.somnium.example.app.configuration

import com.labs.somnium.akka.typed.coroutine.ActorExtensions
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(ActorExtensions::class)
class AppConfiguration
