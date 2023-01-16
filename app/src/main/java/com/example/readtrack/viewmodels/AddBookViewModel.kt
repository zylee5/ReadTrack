package com.example.readtrack.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.readtrack.types.NewBook
import com.example.readtrack.types.StoredBook

class AddBookViewModel : ViewModel() {
    val newBook = MutableLiveData<NewBook>().apply {
        this.value = NewBook()
    }

    val editBook = MutableLiveData<NewBook>()
}