package com.example.readtrack.api_models

import com.squareup.moshi.Json
import java.time.LocalDate

data class VolumeInfoApiModel (
    val title: String? = null,
    val subtitle: String? = null,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val publishedDate: LocalDate? = null,
    val description: String? = null,
    val industryIdentifiers: List<IndustryIdentifierApiModel>? = null,
    val pageCount: Int? = null,
    val mainCategory: String? = null,
    val categories: List<String>? = null,
    val imageLinks: ImageLinksApiModel? = null,
    val language: String? = null

//    @Json(ignore = true)
//    val dimensions: DimensionsApiModel? = null,
//    @Json(ignore = true)
//    val printType: String? = null,
//    @Json(ignore = true)
//     val averageRating: Double? = null,
//    @Json(ignore = true)
//     val ratingsCount: Long? = null,
//    @Json(ignore = true)
//     val contentVersion: String? = null,
//    @Json(ignore = true)
//     val previewLink: String? = null,
//    @Json(ignore = true)
//     val infoLink: String? = null,
//    @Json(ignore = true)
//     val canonicalVolumeLink: String? = null
)