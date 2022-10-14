package com.example.readtrack.util

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.databinding.InverseMethod
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "Converter"

object Converters {
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

    /***
     * Convert LocalDate to string
     * Direction: view model to view
     */
    @InverseMethod("stringToDate") // Specify this function is the inverse method of "stringToDate"
    @JvmStatic
    fun dateToString(
        newValue: LocalDate?
     ): String? = newValue?.format(dateFormatter)

    /***
     * Convert string to LocalDate
     * Direction: view to view model
     */
    @JvmStatic
    fun stringToDate(
        newValue: String?
    ): LocalDate? = newValue?.let { LocalDate.parse(it, dateFormatter) }

    /***
     * Convert a date range to string
     * Direction: view model to view
     */
    @InverseMethod("dateRangeStrToDate")
    @JvmStatic
    fun dateRangeToString(
        dateRange: Pair<LocalDate, LocalDate>?
    ): String? {
        val startedDate = dateRange?.first
        val finishedDate = dateRange?.second

        val startedDateStr = startedDate?.format(dateFormatter)
        val finishedDateStr = finishedDate?.format(dateFormatter)

        return if (!startedDateStr.isNullOrEmpty()
            && !finishedDateStr.isNullOrEmpty()) {
            "$startedDateStr - $finishedDateStr"
        } else {
            null
        }
    }

    /***
     * Convert a string to date range
     * Direction: view to view model
     */
    @JvmStatic
    fun dateRangeStrToDate(
        dateRangeStr: String?
    ): Pair<LocalDate, LocalDate>? {
        val parts = dateRangeStr?.split(" - ")
        val startedDateStr = parts?.get(0)
        val finishedDateStr = parts?.get(1)

        val startedDate = startedDateStr?.let { LocalDate.parse(it, dateFormatter) }
        val finishedDate = finishedDateStr?.let { LocalDate.parse(it, dateFormatter) }

        return if (startedDate != null
            && finishedDate != null) {
            Pair(startedDate, finishedDate)
        } else {
            null
        }
    }
}