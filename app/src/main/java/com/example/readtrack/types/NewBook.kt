package com.example.readtrack.types

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
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
    _name: String = "",
    _authorName: String = "",
    _genre: String = "",
    _startedDate: LocalDate? = null,
    _dateRange: Pair<LocalDate, LocalDate>? = null,
    _status: Status = Status.NONE,
    _rating: Float = 0f,
    _hasCoverImg: ObservableBoolean = ObservableBoolean(coverUri.isNotEmpty())
): BaseObservable() {
    @Bindable
    var name = _name
        set(value) {
            // Important: Only notify observer when the value changes
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.name)
            }
        }
    @Bindable
    var authorName = _authorName // TODO: Possibly a data class
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.author)
            }
        }
    @Bindable
    var genre = _genre
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.genre)
            }
        }
    @Bindable
    var startedDate = _startedDate
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.startedDate)
            }
        }
    @Bindable
    var dateRange = _dateRange
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.dateRange)
            }
        }
    @Bindable
    var status = _status
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.status)
            }
        }
    @Bindable
    var rating = _rating // android:rating receives float
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.rating)
            }
        }
    @Bindable
    var hasCoverImg = _hasCoverImg
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