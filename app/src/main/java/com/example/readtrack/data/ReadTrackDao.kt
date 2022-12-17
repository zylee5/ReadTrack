package com.example.readtrack.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.readtrack.types.StoredBook

@Dao
abstract class ReadTrackDao {
    @Insert
    abstract suspend fun insertBook(book: StoredBook): Long

    @Query("SELECT * from books")
    abstract fun getAllStoredBooks(): LiveData<List<StoredBook>>

    @Delete
    abstract suspend fun deleteBook(book: StoredBook)
}
