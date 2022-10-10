package com.example.readtrack.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.readtrack.data.ReadTrackDatabase
import com.example.readtrack.data.ReadTrackRepository
import com.example.readtrack.types.StoredBook

class BookShelfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReadTrackRepository
    val storedBooks: LiveData<List<StoredBook>>

    init {
        repository = ReadTrackDatabase
            .getDatabase(application)
            .readTrackDao()
            .let { dao ->
                ReadTrackRepository.getInstance(dao)
            }
        storedBooks = repository.getAllStoredBooks()
    }

    suspend fun addBook(book: StoredBook) {
        repository.insertBook(book)
    }
}