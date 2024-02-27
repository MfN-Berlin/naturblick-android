package berlin.mfn.naturblick.backend

import android.content.Context
import android.util.Log
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.backend.BackendApi.EmptyMedia
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.AndroidDeviceId
import berlin.mfn.naturblick.utils.MediaType
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.*

private val contentType = "application/json".toMediaType()

private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .client(client)
    .baseUrl(BuildConfig.BACKEND_URL)
    .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(contentType))
    .build()

interface BackendApiService {

    @POST("signUp")
    @FormUrlEncoded
    suspend fun signUp(
        @Field("deviceIdentifier") deviceId: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Unit

    @POST("signIn")
    @FormUrlEncoded
    suspend fun signIn(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): TokeResponse

    @PUT("device-connect")
    suspend fun deviceConnect(
        @Body connectDevice: ConnectDevice,
        @Header("Authorization") bearerToken: String
    )

    @GET("account/activate/{token}")
    suspend fun activateAccount(@Path("token") token: String)

    @FormUrlEncoded
    @POST("password/forgot")
    suspend fun forgotPassword(@Field("email") email: String)

    @FormUrlEncoded
    @POST("password/reset/{token}")
    suspend fun resetPassword(@Path("token") token: String, @Field("password") password: String)

    @GET("account/delete-count")
    suspend fun deleteCount(
        @Header("Authorization") bearerToken: String,
        @Query("deviceIdentifier") deviceIdentifier: List<String>
    ): DeleteCount

    @FormUrlEncoded
    @POST("account/delete")
    suspend fun delete(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    )

    @PUT("obs/androidsync")
    @Multipart
    suspend fun sync(
        @Part parts: List<MultipartBody.Part>,
        @Header("X-MfN-Device-Id") deviceId: String?,
        @Header("Authorization") bearerToken: String?
    ): ObservationResponse

    @PUT("device")
    suspend fun register(@Body registerDevice: RegisterDevice): Unit

    @GET("specgram/{mediaId}")
    suspend fun specgram(
        @Path("mediaId") mediaId: UUID,
        @Header("X-MfN-Device-Id") deviceId: String?,
        @Header("Authorization") bearerToken: String?
    ): ResponseBody

    @GET("media/{mediaId}")
    @Streaming
    suspend fun media(
        @Path("mediaId") mediaId: UUID,
        @Header("X-MfN-Device-Id") deviceId: String?,
        @Header("Authorization") bearerToken: String?
    ): ResponseBody

    @PUT("upload-media")
    @Multipart
    suspend fun uploadMedia(
        @Query("deviceIdentifier") deviceIdentifier: String,
        @Query("mediaId") mediaId: UUID,
        @Part file: MultipartBody.Part,
        @Header("X-MfN-Device-Id") deviceId: String
    ): ResponseBody
}

data class Chunk(
    val parts: List<MultipartBody.Part>,
    val operations: List<ObservationOperation>,
    val size: Int
) {
    private fun accumulateFile(
        operation: ObservationOperation,
        file: MultipartBody.Part,
        size: Int
    ): Chunk =
        copy(
            parts = parts.plusElement(file),
            operations = operations.plusElement(operation),
            size = this.size + size
        )

    fun accumulateOperation(operation: ObservationOperation): Chunk =
        if (operation is PatchOperation && operation.mediaId == EmptyMedia) {
            copy(operations = operations.plusElement(operation.copy(mediaId = null)))
        } else {
            copy(operations = operations.plusElement(operation))
        }

    fun createRequest(deviceId: String, syncId: Long?): List<MultipartBody.Part> {
        val request = ObservationOperationsRequest(operations, SyncInfo(deviceId, syncId))
        val formData =
            MultipartBody.Part.createFormData("operations", Json.encodeToString(request))
        return parts.plusElement(formData)
    }

    fun addFile(
        context: Context,
        operation: ObservationOperation,
        id: UUID,
        filename: String,
        mime: String
    ): Chunk {
        if (id == EmptyMedia) {
            return this
        } else {
            val byteArray = File(context.filesDir, filename)
                .inputStream()
                .use {
                    it.readBytes()
                }
            val body = MultipartBody.Part.createFormData(
                id.toString(),
                filename,
                byteArray.toRequestBody(
                    mime.toMediaTypeOrNull(),
                    0,
                    byteArray.size
                )
            )
            return accumulateFile(operation, body, byteArray.size)
        }
    }
}

interface PublicBackendApiService {

    suspend fun triggerSync(context: Context) {
        val observationsDb = ObservationDb.getDb(context)
        val operationDao = observationsDb.operationDao()
        val syncDao = observationsDb.syncDao()

        try {
            sync(
                context,
                operationDao, syncDao
            )
        } finally {
            operationDao.refreshObservations()
        }
    }

    private suspend fun sync(
        context: Context,
        operationDao: OperationDao,
        syncDao: SyncDao
    ) {
        val contentResolver = context.contentResolver
        val operations = operationDao.getOperations()
        val deviceId = AndroidDeviceId.deviceId(contentResolver)

        /*
        * Fold splits operations into chunks where each chunk does not upload more
        * than MAX_CHUNK_SIZE. The chunks are sent directly within the fold
        * (i.e. it has side effects) since accumulating them would require keeping
        * all media in memory. When the fold is done, the last accumulated request
        * still has to be sent. That is why the fold itself is wrapped in a syncChunk call.
        */
        syncChunk(
            context,
            operations.fold(Chunk(emptyList(), emptyList(), 0)) { acc, operation ->
                val chunk = when (operation) {
                    is UploadMediaOperation -> if (!operation.uploaded) {
                        acc.addFile(
                            context,
                            operation,
                            operation.mediaId,
                            operation.filename,
                            operation.mime
                        )
                    } else {
                        // File was already uploaded so we can safely remove it
                        File(context.filesDir, operation.filename).delete()
                        acc
                    }
                    is UploadThumbnailMediaOperation ->
                        acc.addFile(
                            context,
                            operation,
                            operation.mediaId,
                            operation.filename,
                            MediaType.JPG.mime
                        )
                    else -> {
                        acc.accumulateOperation(operation)
                    }
                }
                if (chunk.size > MAX_CHUNK_SIZE) {
                    syncChunk(context, chunk, deviceId, operationDao, syncDao)
                    Chunk(emptyList(), emptyList(), 0)
                } else {
                    chunk
                }
            },
            deviceId, operationDao, syncDao
        )
    }

    private suspend fun syncChunk(
        context: Context,
        chunk: Chunk,
        deviceId: String,
        operationDao: OperationDao,
        syncDao: SyncDao
    ) {
        val sync = syncDao.get()
        Log.i(TAG, "${chunk.operations} $deviceId")
        val (deviceIdHeader, bearerToken) = AndroidDeviceId.authHeaders(context)
        val observationResponse =
            BackendApi.service.sync(
                chunk.createRequest(deviceId, sync?.syncId),
                deviceId = deviceIdHeader,
                bearerToken = bearerToken
            )
        operationDao.updateBackendObservations(
            observationResponse.data,
            observationResponse.partial,
            chunk.operations.map { it.operationId }
        )

        // Delete local copies of all uploaded media files (not thumbnails)
        chunk.operations.filterIsInstance<UploadMediaOperation>().forEach {
            File(context.filesDir, it.filename).delete()
        }

        syncDao.newSync(observationResponse.syncId)
    }

    suspend fun register(registerDevice: RegisterDevice) =
        BackendApi.service.register(registerDevice)

    suspend fun specgram(mediaId: UUID, context: Context): ByteArray {
        val (deviceIdHeader, bearerToken) = AndroidDeviceId.authHeaders(context)
        return BackendApi.service.specgram(
            mediaId,
            deviceId = deviceIdHeader,
            bearerToken = bearerToken
        ).bytes()
    }

    suspend fun media(mediaId: UUID, context: Context, file: File): File {
        val (deviceIdHeader, bearerToken) = AndroidDeviceId.authHeaders(context)
        return BackendApi.service.media(
            mediaId,
            deviceId = deviceIdHeader,
            bearerToken = bearerToken
        ).byteStream().use { input ->
            withContext(Dispatchers.IO) {
                try {
                    file.createNewFile()
                    file.outputStream().use { output ->
                        input.copyTo(output)
                        file
                    }
                } catch (e: Exception) {
                    file.delete()
                    throw e
                }
            }
        }
    }

    suspend fun uploadMedia(context: Context, mediaId: UUID, file: File, mediaType: MediaType) {
        val byteArray = file
            .inputStream()
            .buffered()
            .use {
                it.readBytes()
            }
        val body = MultipartBody.Part.createFormData(
            "file",
            file.name,
            byteArray.toRequestBody(
                mediaType.mime.toMediaTypeOrNull(),
                0,
                byteArray.size
            )
        )
        val deviceId = AndroidDeviceId.deviceId(context.contentResolver)
        BackendApi.service.uploadMedia(
            deviceIdentifier = deviceId,
            mediaId = mediaId,
            file = body,
            deviceId = deviceId
        )
    }

    suspend fun signIn(context: Context, email: String, password: String) {
        val response = BackendApi.service.signIn(email, password)
        Settings.setToken(context, response.accessToken, email)

        // Connect all device IDs associated with this app
        val deviceId = AndroidDeviceId.deviceId(context.contentResolver)
        val deviceIds = Settings.getAllDeviceIds(context, deviceId)
        for (id in deviceIds) {
            BackendApi.service.deviceConnect(ConnectDevice(id), "Bearer ${response.accessToken}")
        }
        // Clear sync id to force full update from server
        // so that observations from other devices are fetched
        ObservationDb.getDb(context).syncDao().deleteAll()
        AndroidDeviceId.invalidateToken()
        SyncWorker.triggerBackgroundSync(context)
    }

    suspend fun signUp(context: Context, email: String, password: String) {
        BackendApi.service.signUp(
            deviceId = AndroidDeviceId.deviceId(context.contentResolver),
            email = email,
            password = password
        )
    }

    suspend fun activateAccount(token: String) {
        BackendApi.service.activateAccount(token)
    }

    suspend fun forgotPassword(email: String) {
        BackendApi.service.forgotPassword(email)
    }

    suspend fun resetPassword(token: String, password: String) {
        BackendApi.service.resetPassword(token, password)
    }

    suspend fun deleteCount(context: Context, bearerToken: String): DeleteCount {
        val deviceId = AndroidDeviceId.deviceId(context.contentResolver)
        val deviceIds = Settings.getAllDeviceIds(context, deviceId)
        return BackendApi.service.deleteCount(bearerToken, deviceIds.toList())
    }

    suspend fun delete(context: Context, email: String, password: String) {
        BackendApi.service.delete(email, password)
        Settings.clearEmail(context)
        // Clear sync id to force full update from server
        // so that observations from other devices are removed
        ObservationDb.getDb(context).syncDao().deleteAll()
        SyncWorker.triggerBackgroundSync(context)
    }

    companion object {
        const val MAX_CHUNK_SIZE = 5 * 1024 * 1024 // 5 MB
        const val MAX_RESOLUTION = 2048
        const val TAG = "PublicBackendApiService"
    }
}

object PublicBackendApi {
    val service: PublicBackendApiService by lazy { object : PublicBackendApiService {} }
}

private object BackendApi {
    val EmptyMedia = UUID.fromString("273e0cc9-f2bd-4b78-a5ab-5206ff7ece33")
    val service: BackendApiService by lazy { retrofit.create(BackendApiService::class.java) }
}
