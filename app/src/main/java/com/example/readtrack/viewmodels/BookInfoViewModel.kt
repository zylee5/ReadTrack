package com.example.readtrack.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.readtrack.types.BookFromService
import com.example.readtrack.types.Status
import com.example.readtrack.util.PropertyAwareMutableLiveData
class BookInfoViewModel: ViewModel() {
    val bookFromService = PropertyAwareMutableLiveData<BookFromService>()

    val isBookReadyToAdd = MediatorLiveData<Boolean>().apply {
        addSource(bookFromService) {
            value = isStatusInfoComplete(it)
        }
    }

    val isBookLocallyStored = MutableLiveData(false)
    val isAddToShelfBtnClicked = MutableLiveData(false)
    private fun isStatusInfoComplete(
        bookFromService: BookFromService
    ): Boolean = when(bookFromService.status) {
        Status.READ -> {
            bookFromService.dateRange != null
                    && bookFromService.rating > 0
        }
        Status.READING -> {
            bookFromService.startedDate != null
                    && bookFromService.rating > 0
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