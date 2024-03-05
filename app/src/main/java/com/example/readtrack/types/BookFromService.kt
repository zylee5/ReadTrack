package com.example.readtrack.types

import android.os.Parcelable
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.readtrack.BR
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class BookFromService(
    val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val authors: String? = null, // from List
    val publisher: String? = null,
    val publishedDate: LocalDate? = null, // from String
    val description: String? = null,
    val isbn: String? = null, // from IndustryIdentifier
    val pageCount: Int? = null,
    val genres: String? = null, // from mainCategory and categories
    val url: String? = null, // from imageLinks
    val language: String? = null,
    var readStatus: Status = Status.NONE,
    var readStartedDate: LocalDate? = null,
    var readDateRange: Pair<LocalDate, LocalDate>? = null,
    var readRating: Float = 0f
): Parcelable, BaseObservable() {
    @IgnoredOnParcel
    @Bindable
    var status = readStatus
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.status)
            }
        }

    @Bindable
    var startedDate = readStartedDate
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.startedDate)
            }
        }
    @Bindable
    var dateRange = readDateRange
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.dateRange)
            }
        }
    @Bindable
    var rating = readRating // android:rating receives float
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.rating)
            }
        }

    fun toStoredBook(id: Long = 0) = StoredBook(
        bookId = id,
        bookIdFromService = this.id,
        coverUri = this.url ?: "",
        title = this.title ?: "",
        authors = this.authors ?: "",
        pageCount = this.pageCount ?: 0,
        description = this.description ?: "",
        genres = this.genres ?: "",
        isbn = this.isbn ?: "",
        publisher = this.publisher ?: "",
        publicationDate = this.publishedDate,
        language = this.language ?: "",
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

    fun updateBookWithStatus(anotherBook: StoredBook) {
        this.status = anotherBook.status
        when (this.status) {
            Status.READ -> {
                this.dateRange = Pair(anotherBook.startedDate!!, anotherBook.finishedDate!!)
                this.rating = anotherBook.rating!!
            }
            Status.READING -> {
                this.startedDate = anotherBook.startedDate
                this.rating = anotherBook.rating!!
            }
            else -> {

            }
        }
    }
}