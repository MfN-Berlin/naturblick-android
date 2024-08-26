/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.strapi

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

private val contentType = MediaType.get("application/json")

private val client = OkHttpClient.Builder()
    .readTimeout(90, TimeUnit.SECONDS)
    .build()

interface KtorApiService {

    @GET("app-content/db")
    suspend fun getFile(): ResponseBody

    @GET("app-content/character-values")
    suspend fun getCharacterValues(): List<CharacterValue>
}

object KtorApi {
    fun service(baseUrl: String): KtorApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
            .client(client)
            .build()
        return retrofit.create(KtorApiService::class.java)
    }
}
