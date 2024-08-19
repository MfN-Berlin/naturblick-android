/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.strapi

import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.TimeUnit


private val client = OkHttpClient.Builder()
    .readTimeout(90, TimeUnit.SECONDS)
    .build()

interface KtorApiService {
    @GET("ktor-strapidb")
    suspend fun getFile(): ResponseBody
}

object KtorApi {
    fun service(baseUrl: String): KtorApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .build()
        return retrofit.create(KtorApiService::class.java)
    }
}
