package com.example.readtrack.viewmodels

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bumptech.glide.Glide.init
import com.example.readtrack.data.ReadTrackDatabase
import com.example.readtrack.data.ReadTrackRepository
import com.example.readtrack.types.BookFromService
import com.example.readtrack.types.getErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SearchBookViewModel(application: Application): AndroidViewModel(application) {
    private val repository: ReadTrackRepository
    private val queryText = MutableStateFlow("")
    var booksFromService: Flow<PagingData<BookFromService>>

    init {
        repository = ReadTrackDatabase.getDatabase(application)
            .readTrackDao()
            .let { dao ->
                ReadTrackRepository.getInstance(dao)
            }
        booksFromService = queryText
            .flatMapLatest { query ->
                // Do not search for blank query
                if (query.isNotBlank()) getBooksFromService(query)
                else emptyFlow()
            }
    }

    fun searchBooksFromService(query: String) {
        queryText.value = query
    }

    private fun getBooksFromService(query: String): Flow<PagingData<BookFromService>> {
        return repository.getBooksFromService(query).cachedIn(viewModelScope)
    }
}