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
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private val contentType = MediaType.get("application/json")

private val client = OkHttpClient.Builder()
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

interface StrapiApiService {
    @GET("character-values?_sort=id:DESC")
    suspend fun getCharacterValues(
        @Query("_start") offset: Int?,
        @Query("_limit") limit: Int?
    ): List<CharacterValue>

    @GET("{path}")
    suspend fun getFile(@Path("path") path: String): ResponseBody
}

object StrapiApi {
    fun service(baseUrl: String): StrapiApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
            .client(client)
            .build()
        return retrofit.create(StrapiApiService::class.java)
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun <T> getAll(p: suspend (Int, Int) -> List<T>): List<T> {
        var page = 0
        return buildList {
            do {
                val data = p(page * 1000, 1000)
                addAll(data)
                page++
            } while (data.isNotEmpty())
        }
    }
}
