package com.thoughtworks.archgard.scanner.domain.bs

data class BadSmell(
        val id: String,
        val entityName: String,
        val line: Int,
        val description: String,
        val size: Int,
        val type: String)
