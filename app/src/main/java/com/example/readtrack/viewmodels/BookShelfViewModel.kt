package com.example.readtrack.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.LocaleDisplayNames.UiListItem.getComparator
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
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
    val queryText: MutableLiveData<String> = MutableLiveData()

    init {
        repository = ReadTrackDatabase
            .getDatabase(application)
            .readTrackDao()
            .let { dao ->
                ReadTrackRepository.getInstance(dao)
            }

        queryText.value = ""
        // When queryText.value changes, storedBooks changes
        // We set queryText.value to the latest query text retrieved from SearchView
        // so that storedBooks is observed (and thus all actions required are specified) in one place
        storedBooks = Transformations.switchMap(queryText) {query ->
            // if no query text is found, retrieve all book items
            if (query == null || query.isEmpty()) {
                repository.getAllStoredBooks()
            } else {
                repository.getBooksForQuery(query)
            }
        }
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