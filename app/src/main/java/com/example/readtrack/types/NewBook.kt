package com.example.readtrack.types

import androidx.databinding.ObservableBoolean
import java.time.LocalDate


data class NewBook(
    var name: String = "",
    var authorName: String = "", // TODO: Possibly a data class
    var genre: String = "",
    var startedDate: LocalDate,
    var finishedDate: LocalDate,
    var status: String = Status.READ.string,
    var rating: Float = (-1).toFloat(), // android:rating receives float
//    var startingDateAllowed: ObservableBoolean =
//        ObservableBoolean(status != Status.WANT_TO_READ.string),
//    var finishingDateAllowed: ObservableBoolean =
//        ObservableBoolean(status != Status.WANT_TO_READ.string),
//    var ratingAllowed: ObservableBoolean =
//        ObservableBoolean(status == Status.READ.string)
)