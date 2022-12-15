package com.example.readtrack.util

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.InverseMethod
import com.example.readtrack.MainActivity
import com.example.readtrack.R
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