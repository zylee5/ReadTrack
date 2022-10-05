package com.example.readtrack.types

import java.time.LocalDate

data class StoredBook(
    val name: String,
    val authorName: String,
    val genre: String,
    val startedDate: LocalDate?,
    val finishedDate: LocalDate?,
    val status: Status,
    val rating: Float?
)
