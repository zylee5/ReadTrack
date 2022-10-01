package com.example.readtrack.types

import androidx.databinding.ObservableBoolean


data class NewBook(
    var name: String = "",
    // TODO: Possibly a data class
    var authorName: String = "",
    var genre: String = "",
    // Start and end date

    var status: String = Status.READ.string,
    var rating: Float = (-1).toFloat(), // android:rating receives float
//    var startingDateAllowed: ObservableBoolean =
//        ObservableBoolean(status != Status.WANT_TO_READ.string),
//    var finishingDateAllowed: ObservableBoolean =
//        ObservableBoolean(status != Status.WANT_TO_READ.string),
//    var ratingAllowed: ObservableBoolean =
//        ObservableBoolean(status == Status.READ.string)
)