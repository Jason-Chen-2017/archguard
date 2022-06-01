package org.archguard.scanner.core

import kotlinx.serialization.Serializable

@Serializable
data class AnalyserSpec(
    val identifier: String,
    val host: String,
    val version: String,
    var jar: String,
    val className: String, // calculate via identifier??
    // additional type for slot
    var slotType: String = "",
)
