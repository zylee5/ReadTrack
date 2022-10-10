package com.example.readtrack.data

import com.example.readtrack.types.StoredBook

class ReadTrackRepository(private val readTrackDao: ReadTrackDao) {
    suspend fun insertBook(book: StoredBook) = readTrackDao.insertBook(book)
    fun getAllStoredBooks() = readTrackDao.getAllStoredBooks()

    companion object {
        @Volatile
        private var INSTANCE: ReadTrackRepository? = null

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