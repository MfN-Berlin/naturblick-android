/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

@file:OptIn(ExperimentalSerializationApi::class)

package berlin.mfn.naturblick.utils

import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.ui.idresult.BackendIdResult
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

private val contentType = "application/json".toMediaType()

private val client = OkHttpClient.Builder()
    .connectTimeout(60.seconds.toJavaDuration())
    .readTimeout(60.seconds.toJavaDuration())
    .addInterceptor(logging)
    .build()

private val json = Json { ignoreUnknownKeys = true }

private val retrofit = Retrofit.Builder()
    .client(client)
    .baseUrl(BuildConfig.BACKEND_URL)
    .addConverterFactory(json.asConverterFactory(contentType))
    .build()

internal interface IdApiServiceInterface {

    @GET("androidimageid?")
    suspend fun imageId(
        @Query("mediaId") mediaId: UUID,
        @Query("x") x: Float?,
        @Query("y") y: Float?,
        @Query("size") size: Float?,
        @Query("original") original: UUID?
    ): List<BackendIdResult>

    @GET("androidsoundid?")
    suspend fun soundId(
        @Query("mediaId") mediaId: UUID,
        @Query("segmentStart") segmStart: Int,
        @Query("segmentEnd") segmEnd: Int
    ): List<BackendIdResult>
}

class IdApiService internal constructor(private val service: IdApiServiceInterface) {
    suspend fun imageId(
        thumbnail: RemoteMediaThumbnail,
        x: Float?,
        y: Float?,
        size: Float?,
        // The media is not required to already be available to the server
        // There is no FK-constraint on this id column
        original: Media?
    ): List<BackendIdResult> =
        service.imageId(thumbnail.id, x, y, size, original?.id)

    suspend fun soundId(
        media: RemoteMedia,
        segmStart: Int,
        segmEnd: Int
    ): List<BackendIdResult> =
        service.soundId(media.id, segmStart, segmEnd)
}

object IdApi {
    val service: IdApiService by lazy {
        IdApiService(retrofit.create(IdApiServiceInterface::class.java))
    }
}
