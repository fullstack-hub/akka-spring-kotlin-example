package com.labs.somnium.logprocessing

import akka.actor.typed.ActorSystem
import java.io.File

class DiskError(msg: String) : Error(msg)
class CorruptedFileException(msg: String, file: File) : Exception(msg)
class DbNodeDownException(msg: String) : Exception(msg)
class DbBrokenConnectionException(msg: String) : Exception(msg)

fun main() {
    val sources = listOf("file:///source1", "file:///source2")
    val databaseUrls = listOf("http://mydatabase1", "http://mydatabase2", "http://mydatabase3")
    val system = ActorSystem.create(LogProcessingSupervisor.create(sources, databaseUrls), "file-watcher-supervisor")
}
