package com.example.readtrack.services

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val URL = "https://www.googleapis.com/books/v1/"
object DefaultGoogleBookApiService {
    private val moshi by lazy {
        Moshi.Builder()
            .add(LocalDateJsonAdapter())
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }
    val apiService: GoogleBookApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GoogleBookApiService::class.java)
    }
}