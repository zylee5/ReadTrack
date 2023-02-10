package com.example.readtrack.services

import com.example.readtrack.api_models.IndustryIdentifierApiModel
import com.example.readtrack.api_models.VolumeApiModel
import com.example.readtrack.types.BookFromService
import java.util.*

/*
fun List<VolumeApiModel>.toBookFromService(): List<BookFromService> =
    this.map { apiModel ->
        BookFromService(
            id = apiModel.id,
            title = apiModel.volumeInfo.title,
            subtitle = apiModel.volumeInfo.subtitle,
            authors = apiModel.volumeInfo.authors?.joinToString(", "),
            publisher = apiModel.volumeInfo.publisher,
            publishedDate = apiModel.volumeInfo.publishedDate,
            description = apiModel.volumeInfo.description,
            isbn = apiModel.volumeInfo.industryIdentifiers?.getISBN(),
            pageCount = apiModel.volumeInfo.pageCount,
            genres = getGenresFromCategories(
                apiModel.volumeInfo.mainCategory,
                apiModel.volumeInfo.categories
            ),
            url = apiModel.volumeInfo.imageLinks?.thumbnail,
            language = getDisplayLanguageName(apiModel.volumeInfo.language)
        )
    }
*/

fun List<IndustryIdentifierApiModel>.getISBN(): String? {
    // Return ISBN_13 only (for now)
    for (id in this) {
        if (id.type != "ISBN_13") continue
        return id.identifier
    }
    return null
}

fun getDisplayLanguageName(targetIso: String?): String? {
    if (targetIso == null) return null
    val targetLocale = Locale(targetIso)
    for (locale in Locale.getAvailableLocales()) {
        if (locale == targetLocale) return locale.displayLanguage
    }
    return null
}

fun getGenresFromCategories(mainCategory: String?, categories: List<String>?): String? =
    if (mainCategory == null && categories == null)
        null
    else if (mainCategory != null)
        "$mainCategory, ${categories?.joinToString(", ")}"
    else
        "${categories?.joinToString(", ")}"

