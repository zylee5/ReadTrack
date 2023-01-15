package com.example.readtrack.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.readtrack.data.ReadTrackDatabase
import com.example.readtrack.data.ReadTrackRepository
import com.example.readtrack.types.NewBook
import com.example.readtrack.types.StoredBook
import java.util.*
import kotlin.Comparator

private const val TAG = "BookShelfViewModel"
class BookShelfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReadTrackRepository
    val storedBooks: LiveData<List<StoredBook>>
    var retrievedNewBook: LiveData<NewBook> = MutableLiveData()

    private val queryText: MutableLiveData<String> = MutableLiveData()

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

    // Call this function when query text is inputted
    // It sets the query to queryText, which then modifies storedBooks
    fun searchByTitleOrAuthor(query: String) {
        queryText.value = query
    }

    fun getBookById(id: Long): Boolean {
        val storedBook = repository.getBookById(id)
        this.retrievedNewBook = Transformations.map(storedBook) {
            it.toNewBook()
        }
        return true
    }

    suspend fun updateBook(updatedBook: StoredBook) {
        repository.updateBook(updatedBook)
    }
}