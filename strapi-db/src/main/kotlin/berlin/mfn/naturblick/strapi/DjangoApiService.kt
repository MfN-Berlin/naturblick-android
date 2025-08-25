/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.strapi

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

private val contentType = MediaType.get("application/json")

private val client = OkHttpClient.Builder()
    .readTimeout(90, TimeUnit.SECONDS)
    .build()

@Serializable
data class DjangoGroup(
    val name: String,
    val image: String?,
    val svg: String?
)

interface DjangoApiService {

    @GET("app-content/db")
    suspend fun getFile(): ResponseBody

    @GET("app-content/character-values")
    suspend fun getCharacterValues(): List<CharacterValue>

    @GET("groups/")
    fun getGroups(): Call<List<DjangoGroup>>
}

object DjangoApi {

    private val jsonFormat =
        Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    fun service(baseUrl: String): DjangoApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonFormat.asConverterFactory(contentType))
            .client(client)
            .build()
        return retrofit.create(DjangoApiService::class.java)
    }
}
