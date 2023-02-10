package com.example.readtrack.api_models

import com.squareup.moshi.Json

data class VolumeListApiModel(
    val items: List<VolumeApiModel>? = null,
    val totalItems: Int? = 0

//    @Json(ignore = true)
//    val kind: String? = null,
)