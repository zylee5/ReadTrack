package com.example.readtrack.data

import android.content.Context
import androidx.room.*
import com.example.readtrack.types.StoredBook

@Database(
    entities = [StoredBook::class],
    version = 1
)
@TypeConverters(RoomConverters::class)
abstract class ReadTrackDatabase : RoomDatabase() {
    abstract fun readTrackDao(): ReadTrackDao

    companion object {
        @Volatile // so that INSTANCE is visible to all threads
        private var INSTANCE: ReadTrackDatabase? = null

        fun getDatabase(context: Context)
        : ReadTrackDatabase =
            INSTANCE ?:
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    ReadTrackDatabase::class.java,
                    "ReadTrackDatabase"
                ).build()
                this.INSTANCE = instance
                instance
            }
    }

}