package com.example.readtrack.services

import com.example.readtrack.BuildConfig
import com.example.readtrack.api_models.VolumeListApiModel
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = BuildConfig.apiKey

interface GoogleBookApiService {
    @GET("volumes")
    suspend fun getVolumesForQuery(
        @Query("q") query: String,
        @Query("startIndex") startIndex: Int,
        @Query("maxResults") maxResults: Int,
        @Query("key") key: String = API_KEY
    ): VolumeListApiModel
}