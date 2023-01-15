package com.example.readtrack.types

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import java.io.File
import java.time.LocalDate


data class NewBook(
    var coverUri: String = "",
    var coverImgFiles: MutableList<File?> = ArrayList(),
    var name: String = "",
    var authorName: String = "", // TODO: Possibly a data class
    var genre: String = "",
    var startedDate: LocalDate? = null,
    var dateRange: Pair<LocalDate, LocalDate>? = null,
    var status: Status = Status.NONE,
    var rating: Float = 0f, // android:rating receives float
    var hasCoverImg: ObservableBoolean = ObservableBoolean(coverUri.isNotEmpty())
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
    fun toStoredBook(id: Long) = StoredBook(
        bookId = id,
        coverUri = this.coverUri,
        name = this.name,
        authorName = this.authorName,
        genre = this.genre,
        startedDate = when (this.status) {
            Status.READ -> {
                this.dateRange?.first
            }
            Status.READING -> {
                this.startedDate
            }
            else -> {
                null
            }
        },
        finishedDate = if (this.status == Status.READ) {
            this.dateRange?.second
        } else {
            null
        },
        status = this.status,
        rating = if (this.status == Status.READ ||
            this.status == Status.READING) {
            this.rating // allow for temporary rating while reading, can change later
        } else {
            0f
        }
    )
}