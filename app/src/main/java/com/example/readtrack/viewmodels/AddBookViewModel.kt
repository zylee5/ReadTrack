package com.example.readtrack.viewmodels

import androidx.lifecycle.*
import com.example.readtrack.types.NewBook
import com.example.readtrack.types.Status
import com.example.readtrack.util.PropertyAwareMutableLiveData

private const val TAG = "AddBookViewModel"
class AddBookViewModel : ViewModel() {
    // When properties in newBook are updated, isFormValid needs to get notified (triggering onChanged)
    val newBook = PropertyAwareMutableLiveData<NewBook>().apply {
        this.value = NewBook()
    }
    val editBook = PropertyAwareMutableLiveData<NewBook>()

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
    val isAddBookFormValid = MediatorLiveData<Boolean>().apply {
        addSource(newBook) {
            value = isNewBookValid(it)
        }
    }

    val isEditBookFormValid = MediatorLiveData<Boolean>().apply {
        addSource(editBook) {
            value = isNewBookValid(it)
        }
    }

    private fun isNewBookValid(
        newBook: NewBook
    ): Boolean = !isTextEmpty(newBook.title)
            && !isTextEmpty(newBook.authors)
            && !isTextEmpty(newBook.genres)
            && when(newBook.status) {
                Status.READ -> {
                    newBook.dateRange != null
                            && newBook.rating > 0
                }
                Status.READING -> {
                    newBook.startedDate != null
                            && newBook.rating > 0
                }
                Status.WANT_TO_READ -> {
                    true
                }
                // NONE
                else -> {
                    false
                }
            }

    private fun isTextEmpty(txt: String): Boolean = txt.trim().isEmpty()
}