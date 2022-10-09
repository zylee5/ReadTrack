package com.example.readtrack.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    // StoredBook has LocalDate instance as properties
    // which cannot be converted directly by Room
    @TypeConverter
    fun dateToString(localDate: LocalDate?) = localDate?.format(dateFormatter)

    @TypeConverter
    fun stringToDate(dateString: String?) = dateString?.let {
        LocalDate.parse(it, dateFormatter)
    }
}