/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

@file:OptIn(ExperimentalSerializationApi::class)

package berlin.mfn.naturblick.strapi

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

private val contentType = MediaType.get("application/json")

private val client = OkHttpClient.Builder()
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

interface StrapiApiService {
    @GET("{path}")
    suspend fun getFile(@Path("path") path: String): ResponseBody
}

object StrapiApi {
    private val jsonFormat =
        Json { ignoreUnknownKeys = true }

    fun service(baseUrl: String): StrapiApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonFormat.asConverterFactory(contentType))
            .client(client)
            .build()
        return retrofit.create(StrapiApiService::class.java)
    }
}
