package com.example.readtrack.types

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "books")
data class StoredBook(
    @PrimaryKey(autoGenerate = true)
    var bookId: Long = 0,
    val name: String,
    val authorName: String,
    val genre: String,
    val startedDate: LocalDate?,
    val finishedDate: LocalDate?,
    val status: Status,
    val rating: Float?
)
