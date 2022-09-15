package com.example.readtrack.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.readtrack.types.Book

class AddBookViewModel : ViewModel() {
    val book = MutableLiveData<Book>().apply {
        this.value = Book()
    }
}