package com.example.readtrack.types

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "books")
data class StoredBook(
    @PrimaryKey(autoGenerate = true)
    var bookId: Long = 0,
    val bookIdFromService: String? = null,
    val coverUri: String,
    val title: String,
    val volume: String = "",
    val authors: String,
    val pageCount: Int = 0,
    val description: String = "",
    val genres: String,
    val isbn: String = "",
    val publisher: String = "",
    val publicationDate: LocalDate? = null,
    val language: String = "",
    val startedDate: LocalDate?,
    val finishedDate: LocalDate?,
    val status: Status,
    val rating: Float?
) {
    fun toNewBook() = NewBook(
        coverUri = this.coverUri,
        title = this.title,
        authors = this.authors,
        genres = this.genres,
        startedDate = if (this.status == Status.READING) {
            this.startedDate
        } else {
            null
        },
        dateRange = if (this.startedDate != null && this.finishedDate != null && this.status == Status.READ) {
            Pair(this.startedDate, this.finishedDate)
        } else {
            null
        },
        status = this.status,
        rating = this.rating ?: 0f
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