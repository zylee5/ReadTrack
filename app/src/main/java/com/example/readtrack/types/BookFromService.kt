package com.example.readtrack.types

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class BookFromService(
    val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val authors: String? = null, // from List
    val publisher: String? = null,
    val publishedDate: LocalDate? = null, // from String
    val description: String? = null,
    val isbn: String? = null, // from IndustryIdentifier
    val pageCount: Int? = null,
    val genres: String? = null, // from mainCategory and categories
    val url: String? = null, // from imageLinks
    val language: String? = null
): Parcelable
