package com.example.readtrack.api_models

import com.example.readtrack.services.getDisplayLanguageName
import com.example.readtrack.services.getGenresFromCategories
import com.example.readtrack.services.getISBN
import com.example.readtrack.types.BookFromService
import com.squareup.moshi.Json

data class VolumeApiModel (
    val id: String,
    val volumeInfo: VolumeInfoApiModel

//    @Json(ignore = true)
//    val kind: String? = null,
//    @Json(ignore = true)
//    val etag: String? = null,
//    @Json(ignore = true)
//    val selfLink: String? = null,
//    @Json(ignore = true)
//    val userInfo: UserInfoApiModel? = null,
//    @Json(ignore = true)
//    val saleInfo: SaleInfoApiModel? = null,
//    @Json(ignore = true)
//    val accessInfo: AccessInfoApiModel? = null,
//    @Json(ignore = true)
//    val searchInfo: SearchInfoApiModel? = null
) {
    fun toBookFromService(): BookFromService =
        BookFromService(
            id = this.id,
            title = this.volumeInfo.title,
            subtitle = this.volumeInfo.subtitle,
            authors = this.volumeInfo.authors?.joinToString(", "),
            publisher = this.volumeInfo.publisher,
            publishedDate = this.volumeInfo.publishedDate,
            description = this.volumeInfo.description,
            isbn = this.volumeInfo.industryIdentifiers?.getISBN(),
            pageCount = this.volumeInfo.pageCount,
            genres = getGenresFromCategories(
                this.volumeInfo.mainCategory,
                this.volumeInfo.categories
            ),
            url = this.volumeInfo.imageLinks?.thumbnail,
            language = getDisplayLanguageName(this.volumeInfo.language)
        )
}