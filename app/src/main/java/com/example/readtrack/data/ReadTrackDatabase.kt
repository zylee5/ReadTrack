package com.example.readtrack.data

import android.content.Context
import androidx.room.*
import com.example.readtrack.types.StoredBook
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [StoredBook::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ReadTrackDatabase : RoomDatabase() {
    abstract fun readTrackDao(): ReadTrackDao

    companion object {
        @Volatile // so that INSTANCE is visible to all threads
        private var INSTANCE: ReadTrackDatabase? = null

        fun getDatabase(context: Context)
        : ReadTrackDatabase {
            if (INSTANCE != null) return INSTANCE as ReadTrackDatabase
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    ReadTrackDatabase::class.java,
                    "ReadTrackDatabase"
                ).build()
                this.INSTANCE = instance
                return instance
            }
        }
    }

}