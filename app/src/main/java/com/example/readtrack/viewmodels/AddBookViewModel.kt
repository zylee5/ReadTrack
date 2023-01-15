package com.example.readtrack.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.readtrack.types.NewBook

class AddBookViewModel : ViewModel() {
    val newBook = MutableLiveData<NewBook>().apply {
        this.value = NewBook()
    }

    val editBook = MutableLiveData<NewBook>()


}