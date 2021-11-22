package com.labs.somnium.logprocessing

class DbCon(private val url: String) {
    fun write(map: Map<String, Any>) {}
    fun close() {}
}
