package com.example.readtrack.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.readtrack.types.StoredBook

@Dao
abstract class ReadTrackDao {
    @Insert
    abstract suspend fun insertBook(book: StoredBook): Long

    @Query("SELECT * FROM books")
    abstract fun getAllStoredBooks(): LiveData<List<StoredBook>>

    @Delete
    abstract suspend fun deleteBook(book: StoredBook)

    @Query("SELECT * FROM books WHERE title LIKE :query OR authors LIKE :query")
    abstract fun getBooksForQuery(query: String): LiveData<List<StoredBook>>

    @Query("SELECT * FROM books WHERE bookId = :id")
    abstract suspend fun getBookById(id: Long): StoredBook

    @Update
    abstract suspend fun updateBook(updatedBook: StoredBook)

    @Query("SELECT * FROM books WHERE bookIdFromService = :idFromService")
    abstract suspend fun getBookByIdFromService(idFromService: String): StoredBook?
}
