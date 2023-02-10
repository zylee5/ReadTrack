package com.example.readtrack.types

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "books")
data class StoredBook(
    @PrimaryKey(autoGenerate = true)
    var bookId: Long = 0,
    val coverUri: String,
    val title: String,
    val volume: String = "", // TODO: no default
    val authors: String,
    val pageCount: Long = 0L, // TODO: no default
    val description: String = "", // TODO: no default
    val genres: String,
    val isbn: String = "", // TODO: no default
    val publisher: String = "", // TODO: no default
    val publicationDate: LocalDate? = null, // TODO: no default
    val language: String = "", // TODO: no default
    val startedDate: LocalDate?,
    val finishedDate: LocalDate?,
    val status: Status,
    val rating: Float?
) {
    fun toNewBook() = NewBook(
        coverUri = this.coverUri,
        _title = this.title,
        _authors = this.authors,
        _genres = this.genres,
        _startedDate = if (this.status == Status.READING) {
            this.startedDate
        } else {
            null
        },
        _dateRange = if (this.startedDate != null && this.finishedDate != null && this.status == Status.READ) {
            Pair(this.startedDate, this.finishedDate)
        } else {
            null
        },
        _status = this.status,
        _rating = this.rating ?: 0f
    )

    class BookNameComparator: Comparator<StoredBook> {
        override fun compare(b1: StoredBook?, b2: StoredBook?): Int {
            if (b1 == null || b2 == null) return 0
            return b1.title.lowercase().compareTo(b2.title.lowercase())
        }
    }

    class AuthorNameComparator: Comparator<StoredBook> {
        override fun compare(b1: StoredBook?, b2: StoredBook?): Int {
            if (b1 == null || b2 == null) return 0
            return b1.authors.lowercase().compareTo(b2.authors.lowercase())
        }
    }

    class GenreComparator: Comparator<StoredBook> {
        override fun compare(b1: StoredBook?, b2: StoredBook?): Int {
            if (b1 == null || b2 == null) return 0
            return b1.genres.lowercase().compareTo(b2.genres.lowercase())
        }
    }

    class StartDateComparator: Comparator<StoredBook> {
        override fun compare(b1: StoredBook?, b2: StoredBook?): Int {
            if (b1 == null || b2 == null || b1.startedDate == null || b2.startedDate == null) return 0
            return b1.startedDate.compareTo(b2.startedDate)
        }
    }

    class FinishedDateComparator: Comparator<StoredBook> {
        override fun compare(b1: StoredBook?, b2: StoredBook?): Int {
            if (b1 == null || b2 == null || b1.finishedDate == null || b2.finishedDate == null) return 0
            return b1.finishedDate.compareTo(b2.finishedDate)
        }
    }

    class StatusComparator: Comparator<StoredBook> {
        override fun compare(b1: StoredBook?, b2: StoredBook?): Int {
            if (b1 == null || b2 == null) return 0
            return b1.status.compareTo(b2.status)
        }
    }

    class RatingComparator: Comparator<StoredBook> {
        override fun compare(b1: StoredBook?, b2: StoredBook?): Int {
            if (b1 == null || b2 == null || b1.rating == null || b2.rating == null) return 0
            return b1.rating.compareTo(b2.rating)
        }
    }


}