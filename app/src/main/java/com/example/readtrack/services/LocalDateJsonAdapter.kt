package com.example.readtrack.services

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

class LocalDateJsonAdapter {
    // Date can be yyyy or yyyy-MM or yyyy-MM-dd
    private val apiDateFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy[-MM][-dd]")
        .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
        .toFormatter()

    @ToJson
    fun localDateToJson(date: LocalDate?): String? =
        date?.format(apiDateFormatter)

    @FromJson
    fun jsonToLocalDate(json: String?): LocalDate? =
        json?.let {
            LocalDate.parse(it, apiDateFormatter)
        }
}