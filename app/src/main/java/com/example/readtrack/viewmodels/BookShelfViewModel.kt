package com.example.readtrack.viewmodels

import androidx.lifecycle.ViewModel
import com.example.readtrack.types.NewBook
import com.example.readtrack.types.StoredBook

class BookShelfViewModel : ViewModel() {
    private lateinit var storedBook: StoredBook

    fun addBook(book: StoredBook) {
        this.storedBook = book
    }

}