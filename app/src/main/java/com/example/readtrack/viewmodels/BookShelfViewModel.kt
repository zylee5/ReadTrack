package com.example.readtrack.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readtrack.data.ReadTrackDatabase
import com.example.readtrack.data.ReadTrackRepository
import com.example.readtrack.types.NewBook
import com.example.readtrack.types.StoredBook

class BookShelfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReadTrackRepository
    private val storedBooks: LiveData<List<StoredBook>>

    init {
        val dao = ReadTrackDatabase
            .getDatabase(application)
            .readTrackDao()
        repository = ReadTrackRepository.getInstance(dao)
        storedBooks = repository.getAllStoredBooks()
    }

    suspend fun addBook(book: StoredBook) {
        repository.insertBook(book)
    }

}