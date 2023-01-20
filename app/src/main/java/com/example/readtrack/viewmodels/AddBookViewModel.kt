package com.example.readtrack.viewmodels

import android.util.Log
import android.view.animation.Transformation
import androidx.lifecycle.*
import com.example.readtrack.R
import com.example.readtrack.types.NewBook
import com.example.readtrack.types.Status
import com.example.readtrack.types.StoredBook
import com.example.readtrack.util.PropertyAwareMutableLiveData
import com.google.android.material.textfield.TextInputEditText

private const val TAG = "AddBookViewModel"
class AddBookViewModel : ViewModel() {
    private val commonErrorMsg = "You must fill in this field"
    // When properties in newBook are updated, isFormValid needs to get notified (triggering onChanged)
    val newBook = PropertyAwareMutableLiveData<NewBook>().apply {
        this.value = NewBook()
    }
    val editBook = MutableLiveData<NewBook>()
    val nameErrorMsg = MutableLiveData("")
    val authorErrorMsg = MutableLiveData("")
    val genreErrorMsg = MutableLiveData("")
    val dateRangeErrorMsg = MutableLiveData("")
    val startedDateErrorMsg = MutableLiveData("")

    /***
     * Check if the add book form is valid
     * Criteria:
     * (1) Status must be selected
     * (2) Name, author, genre must be filled regardless of status
     * (3) Correct date text field must be filled based on selected status
     *     READ: date range, READING: single date
     * (4) Rating cannot be zero for not WANT_TO_READ status
     *
     */
    // Set to true/false based on the properties of NewBook
    val isFormValid = MediatorLiveData<Boolean>().apply {
        addSource(newBook) {
            value = !isTextEmpty(it.name)
                    && !isTextEmpty(it.authorName)
                    && !isTextEmpty(it.genre)
                    && when(it.status) {
                        Status.READ -> {
                            it.dateRange != null
                                    && it.rating > 0
                        }
                        Status.READING -> {
                            it.startedDate != null
                                    && it.rating > 0
                        }
                        Status.WANT_TO_READ -> {
                            true
                        }
                        // NONE
                        else -> {
                            false
                        }
                    }
        }
    }

    fun validateName(name: String) {
        Log.d(TAG, "validateName is called. Name: $name")
        if (isTextEmpty(name)) {
            nameErrorMsg.value = commonErrorMsg
        } else {
            nameErrorMsg.value = ""
        }
    }
    fun validateAuthor(authorName: String) {
        if (isTextEmpty(authorName)) {
            authorErrorMsg.value = commonErrorMsg
        } else {
            authorErrorMsg.value = ""
        }
    }
    fun validateGenre(genre: String) {
        if (isTextEmpty(genre)) {
            genreErrorMsg.value = commonErrorMsg
        } else {
            genreErrorMsg.value = ""
        }
    }
    fun validateDateRange(dateRangeStr: String) {
        if (isTextEmpty(dateRangeStr)) {
            dateRangeErrorMsg.value = commonErrorMsg
        } else {
            dateRangeErrorMsg.value = ""
        }
    }
    fun validateStartedDate(startedDateStr: String) {
        if (isTextEmpty(startedDateStr)) {
            startedDateErrorMsg.value = commonErrorMsg
        } else {
            startedDateErrorMsg.value = ""
        }
    }

    private fun isTextEmpty(txt: String): Boolean = txt.trim().isEmpty()
}