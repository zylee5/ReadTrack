package com.example.readtrack.util

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.databinding.InverseMethod
import com.example.readtrack.MainActivity
import com.example.readtrack.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "Converter"

object BindingConverters {
    private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
    private val apiYearOnlyFormatter = DateTimeFormatter.ofPattern("yyyy") // Can't be used in parsing

    /***
     * Convert LocalDate to string
     * Direction: view model to view
     */
    @InverseMethod("stringToStartedDate") // Specify this function is the inverse method of "stringToDate"
    @JvmStatic
    fun startedDateToString(
        newValue: LocalDate?
     ): String? = newValue?.format(dateFormatter)

    /***
     * Convert string to LocalDate
     * Direction: view to view model
     */
    @JvmStatic
    fun stringToStartedDate(
        newValue: String?
    ): LocalDate? {
        Log.d(TAG, "Started Date String: $newValue")
        return if (newValue?.isNotEmpty() == true){
            LocalDate.parse(newValue, dateFormatter)
        } else {
            null
        }
    }

    // Publication date shows only year
    @JvmStatic
    fun publishedDateToString(
        publishedDate: LocalDate?
    ) = publishedDate?.format(apiYearOnlyFormatter)

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
        Log.d(TAG, "Date Range String: $dateRangeStr")
        if (dateRangeStr?.isNotEmpty() == true) {
            val parts = dateRangeStr.split(" - ")
            val startedDateStr = parts[0]
            val finishedDateStr = parts[1]

            val startedDate = LocalDate.parse(startedDateStr, dateFormatter)
            val finishedDate = LocalDate.parse(finishedDateStr, dateFormatter)

            return if (startedDate != null
                && finishedDate != null) {
                Pair(startedDate, finishedDate)
            } else {
                null
            }
        } else {
            return null
        }
    }

    /***
     * To convert image uri to bitmap drawable
     * Direction: view model to view
     */
    @JvmStatic
    fun uriToBitmap(
        uri: String
    ): BitmapDrawable {
        val context = MainActivity.applicationContext()
        // Moved from AddBookFragment
        val bitmap = ImageUtils.decodeUriStreamToBitmap(
            Uri.parse(uri),
            // Convert the dimension values (dp) to pixels (px) in integers
            context.resources.getDimensionPixelSize(R.dimen.bk_cover_img_width),
            context.resources.getDimensionPixelSize(R.dimen.bk_cover_img_height),
            context
        )
        return BitmapDrawable(context.resources, bitmap)
    }
}