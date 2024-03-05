package com.example.readtrack.types

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableBoolean
import com.example.readtrack.BR
import java.io.File
import java.time.LocalDate

/***
 * An observable class so that property change can be notified
 */
class NewBook(
    @Bindable
    var coverUri: String = "",
    @Bindable
    var coverImgFiles: MutableList<File?> = ArrayList(),
    title: String = "",
    authors: String = "",
    genres: String = "",
    startedDate: LocalDate? = null,
    dateRange: Pair<LocalDate, LocalDate>? = null,
    status: Status = Status.NONE,
    rating: Float = 0f,
    hasCoverImg: ObservableBoolean = ObservableBoolean(coverUri.isNotEmpty())
): BaseObservable() {
    @Bindable
    var title = title
        set(value) {
            // Important: Only notify observer when the value changes
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.name)
            }
        }
    @Bindable
    var authors = authors
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.author)
            }
        }
    @Bindable
    var genres = genres
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.genre)
            }
        }
    @Bindable
    var startedDate = startedDate
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.startedDate)
            }
        }
    @Bindable
    var dateRange = dateRange
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.dateRange)
            }
        }
    @Bindable
    var status = status
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.status)
            }
        }
    @Bindable
    var rating = rating // android:rating receives float
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.rating)
            }
        }
    @Bindable
    var hasCoverImg = hasCoverImg
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.coverImg)
            }
        }

    /***
     * Convert new book instance to stored book instance
     */
    fun toStoredBook(id: Long = 0) = StoredBook(
        bookId = id,
        coverUri = this.coverUri,
        title = this.title,
        authors = this.authors,
        genres = this.genres,
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