package com.example.readtrack.data

import android.nfc.tech.MifareUltralight.PAGE_SIZE
import androidx.lifecycle.Transformations.map
import androidx.paging.*
import com.example.readtrack.api_models.VolumeListApiModel
import com.example.readtrack.services.*
import com.example.readtrack.types.ApiResultStatus
import com.example.readtrack.types.BookFromService
import com.example.readtrack.types.StoredBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

class ReadTrackRepository(private val readTrackDao: ReadTrackDao) {
    suspend fun insertBook(book: StoredBook) = readTrackDao.insertBook(book)
    fun getAllStoredBooks() = readTrackDao.getAllStoredBooks()
    suspend fun deleteBook(book: StoredBook) = readTrackDao.deleteBook(book)
    fun getBooksForQuery(query: String) = readTrackDao.getBooksForQuery(query)
    suspend fun getBookById(id: Long): StoredBook = readTrackDao.getBookById(id)
    suspend fun updateBook(updatedBook: StoredBook) = readTrackDao.updateBook(updatedBook)

    /***
     * Get Flow<PagingData<VolumeApiModel>> and convert it into
     * Flow<PagingData<BookFromService>>
     */
    fun getBooksFromService(query: String): Flow<PagingData<BookFromService>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                maxSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { VolumePagingSource(apiService, query) }
        ).flow.map { pagingData ->
            pagingData.map { apiModel ->
                apiModel.toBookFromService()
            }
        }

    companion object {
        @Volatile
        private var INSTANCE: ReadTrackRepository? = null
        private val apiService = DefaultGoogleBookApiService.apiService
        const val PAGE_SIZE = 10

        fun getInstance(readTrackDao: ReadTrackDao)
        : ReadTrackRepository =
            INSTANCE ?:
            synchronized(this) {
                val instance = ReadTrackRepository(readTrackDao)
                this.INSTANCE = instance
                instance
            }
    }
}