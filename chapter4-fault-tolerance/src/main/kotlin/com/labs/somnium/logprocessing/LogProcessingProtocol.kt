package com.labs.somnium.logprocessing

import java.io.File

interface LogProcessingProtocol
data class LogFile(val file: File) : LogProcessingProtocol
data class Line(val time: Long, val message: String, val messageType: String) : LogProcessingProtocol
