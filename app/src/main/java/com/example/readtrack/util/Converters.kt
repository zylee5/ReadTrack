package com.example.readtrack.util

import android.util.Log
import androidx.databinding.InverseMethod
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "Converter"

object Converters {
    /***
     * Convert LocalDate to string
     * Direction: view model to view
     */
    @InverseMethod("stringToDate") // Specify this function is the inverse method of "stringToDate"
    @JvmStatic
    fun dateToString(
        newValue: LocalDate
     ): String {
        return ""
    }

    /***
     * Convert string to LocalDate
     * Direction: view to view model
     */

    @JvmStatic
    fun stringToDate(
        newValue: String
    ): LocalDate {
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
        Log.d(TAG, LocalDate.parse(newValue, dateFormatter).toString())

        return LocalDate.parse(newValue, dateFormatter)
    }

    /***
     * Convert a date range to string
     * Direction: view model to view
     */
    @InverseMethod("dateRangeStrToDate")
    @JvmStatic
    fun dateRangeToString(
        dateRange: Pair<LocalDate, LocalDate>
    ): String {
        return ""
    }

    /***
     * Convert a string to date range
     * Direction: view to view model
     */
    @JvmStatic
    fun dateRangeStrToDate(
        dateRangeStr: String
    ): Pair<LocalDate, LocalDate> {
        val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

        val parts = dateRangeStr.split(" - ")
        val startedDateStr = parts[0]
        val finishedDateStr = parts[1]

        val startedDate = LocalDate.parse(startedDateStr, dateFormatter)
        val finishedDate = LocalDate.parse(finishedDateStr, dateFormatter)
        Log.d(TAG, Pair(startedDate, finishedDate).toString())

        return Pair(startedDate, finishedDate)
    }
}