package com.example.readtrack.types

data class Book(
    var name: String = "",
    // TODO: Possibly a data class
    var authorName: String = "",
    var genre: String = "",
    // Start and end date

    var status: String = "",
    var rating: Int = -1,
)
