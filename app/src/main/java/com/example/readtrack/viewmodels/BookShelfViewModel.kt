package com.example.readtrack.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.LocaleDisplayNames.UiListItem.getComparator
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import com.example.readtrack.data.ReadTrackDatabase
import com.example.readtrack.data.ReadTrackRepository
import com.example.readtrack.types.StoredBook
import com.example.readtrack.util.SwipeGesture
import kotlinx.coroutines.launch
import java.util.*
import kotlin.Comparator

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

    suspend fun deleteBook(book: StoredBook) {
        repository.deleteBook(book)
    }

    fun sortBooks(comparator: Comparator<StoredBook>) {
        storedBooks.value?.let {
            Collections.sort(it, comparator)
        }
    }
}