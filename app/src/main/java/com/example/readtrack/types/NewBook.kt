package com.example.readtrack.types

import java.time.LocalDate


data class NewBook(
    var name: String = "",
    var authorName: String = "", // TODO: Possibly a data class
    var genre: String = "",
    var startedDate: LocalDate = LocalDate.now(),
    var dateRange: Pair<LocalDate, LocalDate> =
        Pair(LocalDate.now(), LocalDate.now()),
    var status: Status = Status.NONE,
    var rating: Float = (0).toFloat(), // android:rating receives float
//    var startingDateAllowed: ObservableBoolean =
//        ObservableBoolean(status != Status.WANT_TO_READ.string),
//    var finishingDateAllowed: ObservableBoolean =
//        ObservableBoolean(status != Status.WANT_TO_READ.string),
//    var ratingAllowed: ObservableBoolean =
//        ObservableBoolean(status == Status.READ.string)
) {
    /***
     * Convert new book instance to stored book instance
     */
    fun toStoredBook() = StoredBook(
        name = this.name,
        authorName = this.authorName,
        genre = this.genre,
        startedDate = when (this.status) {
            Status.READ -> {
                this.dateRange.first
            }
            Status.READING -> {
                this.startedDate
            }
            else -> {
                null
            }
        },
        finishedDate = if (this.status == Status.READ) {
            this.dateRange.second
        } else {
            null
        },
        status = this.status,
        rating = if (this.status == Status.READ ||
            this.status == Status.READING) {
            this.rating // allow for temporary rating while reading, can change later
        } else {
            (0).toFloat()
        }
    )


}