package com.labs.somnium.logprocessing

import java.io.File

interface FileWatcherProtocol
data class NewFile(val file: File, val timeAdded: Long) : FileWatcherProtocol
data class SourceAbandoned(val uri: String) : FileWatcherProtocol
