package com.example.readtrack.types

import android.util.Half.toFloat

data class Book(
    var name: String = "",
    // TODO: Possibly a data class
    var authorName: String = "",
    var genre: String = "",
    // Start and end date

    var status: String = "",
    var rating: Float = (-1).toFloat() // android:rating receives float
)
