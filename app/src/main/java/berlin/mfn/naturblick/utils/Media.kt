package berlin.mfn.naturblick.utils

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.backend.Coordinates
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.utils.AndroidDeviceId.glideHeaders
import berlin.mfn.naturblick.utils.FileUtil.toFileProviderUri
import berlin.mfn.naturblick.utils.Media.Companion.TAG
import berlin.mfn.naturblick.utils.Media.Companion.afterQContentUri
import berlin.mfn.naturblick.utils.Media.Companion.filename
import berlin.mfn.naturblick.utils.Media.Companion.localMediaIdToFilename
import berlin.mfn.naturblick.utils.Media.Companion.preQFile
import berlin.mfn.naturblick.utils.NetworkResult.Companion.catchNetworkAndServerErrors
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.target.Target
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.ExecutionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

interface MediaMeta : Parcelable {

    val fetchCoordinates: Boolean get() = when (this) {
        is CreatedMediaMeta -> this.coordinates == null
        else -> false
    }

    companion object {
        private fun metaFromUri(context: Context, uri: Uri): Pair<LocalDateTime?, Coordinates?> =
            context.contentResolver.openInputStream(uri).use { inputStream ->
                inputStream?.let { input ->
                    val exif = ExifInterface(input)
                    val dateTimeStr = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
                        ?: exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                        ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                    val exifCreated = try {
                        dateTimeStr?.let {
                            LocalDateTime.parse(
                                it,
                                DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
                            )
                        }
                    } catch (e: DateTimeParseException) {
                        Log.i(TAG, "Failed to parse exif timestamp", e)
                        null
                    }
                    val exifCoordinates = exif.latLong?.let {
                        Coordinates(it[0], it[1])
                    }
                    Pair(exifCreated, exifCoordinates)
                } ?: Pair(null, null)
            }

        fun imported(context: Context, uri: Uri): ImportedMediaMeta {
            val (time, coords) = metaFromUri(context, uri)
            return ImportedMediaMeta(time, coords)
        }

        fun created(context: Context, uri: Uri): CreatedMediaMeta {
            val (_, coords) = metaFromUri(context, uri)
            return CreatedMediaMeta(ZonedDateTime.now(), coords)
        }
    }
}

@Parcelize
data class ImportedMediaMeta(val created: LocalDateTime?, val coordinates: Coordinates?) : MediaMeta

@Parcelize
data class CreatedMediaMeta(val created: ZonedDateTime, val coordinates: Coordinates?) : MediaMeta

@Parcelize
object ExistingMedia : MediaMeta

@Parcelize
enum class MediaType(val extension: String, val mime: String) : Parcelable {
    JPG("jpg", "image/jpeg"),
    MP4("mp4", "audio/mp4")
}

sealed interface Media : Parcelable {
    val type: MediaType
    val id: UUID
    val meta: MediaMeta
    fun availableWithoutPermission(context: Context, yes: (Uri) -> Unit, no: () -> Unit)
    suspend fun fetchUri(readPermissionGranted: Boolean, context: Context): NetworkResult<Uri>
    suspend fun syncRemote(
        context: Context,
        localMediaId: String? = null,
        obsIdent: String? = null
    ): RemoteMedia

    suspend fun upload(
        context: Context
    ): NetworkResult<RemoteMedia>

    fun uploadFile(context: Context) =
        File(context.filesDir, "upload_${filename(type, id)}")

    companion object {
        const val TAG = "Media"
        private const val SUBDIR = "Naturblick"
        private val PREFIXED_SUBDIR = "${File.separator}$SUBDIR"
        const val JPEG_QUALITY = 77
        private fun externalDirectory(type: MediaType): String? = when (type) {
            MediaType.JPG -> Environment.DIRECTORY_PICTURES
            MediaType.MP4 -> if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                Environment.DIRECTORY_MUSIC
            } else {
                Environment.DIRECTORY_RECORDINGS
            }
        }

        private fun mediaDirectory(type: MediaType): String =
            when (val dir = externalDirectory(type)) {
                null -> SUBDIR
                else -> dir + PREFIXED_SUBDIR
            }

        private var imageDirectory: File? = null
        private var soundDirectory: File? = null

        private fun preQCreateDirectory(type: MediaType): File {
            val storageDir = Environment.getExternalStoragePublicDirectory(externalDirectory(type))
            val dir = File(storageDir, SUBDIR)
            dir.mkdirs()
            return dir
        }

        private fun preQStorageDirectory(type: MediaType): File {
            return when (type) {
                MediaType.JPG -> imageDirectory ?: run {
                    val dir = preQCreateDirectory(type)
                    imageDirectory = dir
                    dir
                }
                MediaType.MP4 -> soundDirectory ?: run {
                    val dir = preQCreateDirectory(type)
                    soundDirectory = dir
                    dir
                }
            }
        }

        fun filename(type: MediaType, id: UUID): String =
            "naturblick_$id.${type.extension}"

        fun preQFile(type: MediaType, id: UUID) =
            File(preQStorageDirectory(type), filename(type, id))

        fun createEmpty(type: MediaType, context: Context): EmptyLocalMedia {
            val id = UUID.randomUUID()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return createUriAfterQ(type, id, context)
            }
            return createUriForPreQ(type, id, context)
        }

        /**
         * Create File Uri for Pre Q devices
         * With using File Provider
         */
        private fun createUriForPreQ(type: MediaType, id: UUID, context: Context): EmptyLocalMedia {
            val file = preQFile(type, id)
            file.createNewFile()
            return EmptyLocalMedia(type, id, file.toFileProviderUri(context))
        }

        fun afterQContentUri(type: MediaType): Uri = when (type) {
            MediaType.JPG -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaType.MP4 -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        /**
         * Create File Uri for after Q Devices
         * With using Media Store
         */
        private fun createUriAfterQ(type: MediaType, id: UUID, context: Context): EmptyLocalMedia {
            val name = filename(type, id)
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, type.mime)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH, mediaDirectory(type)
                )
            }
            val uri =
                resolver.insert(afterQContentUri(type), contentValues)
                    ?: throw FileException("Failed to create MediaStore record for $name")
            return EmptyLocalMedia(type, id, uri)
        }

        private fun parseLocalMediaId(localMediaId: String): Pair<Long, String>? {
            // Format: "<content-id>:<path>"
            // content-id = _ID
            val parts = localMediaId.split(';')
            return if (parts.size == 2) {
                Pair(parts[0].toLong(), parts[1])
            } else {
                null
            }
        }

        fun localMediaIdToId(localMediaId: String): Long? =
            parseLocalMediaId(localMediaId)?.first

        fun localMediaIdToFilename(localMediaId: String): String? =
            parseLocalMediaId(localMediaId)?.second?.let {
                val pathParts = it.split("/")
                if (pathParts.size > 2) {
                    pathParts.last()
                } else {
                    null
                }
            }

        private fun localMediaIdToUri(context: Context, localMediaId: String): Uri? =
            localMediaIdToId(localMediaId)?.let {
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    it
                )
                try {
                    // Check that URI exists
                    context.contentResolver.openInputStream(uri).use {
                        uri
                    }
                } catch (_: FileNotFoundException) {
                    null
                }
            }

        private fun legacyUri(
            context: Context,
            obsIdent: String?,
            localMediaId: String?
        ): Pair<String, Uri>? =
            (
                localMediaId
                    ?: obsIdent?.let {
                        OldDB.findLocalMediaId(context, obsIdent)
                    }
                )?.let { resolvedMediaId ->
                localMediaIdToUri(context, resolvedMediaId)?.let {
                    Pair(resolvedMediaId, it)
                }
            }

        suspend fun createLegacyImage(
            context: Context,
            obsIdent: String?,
            localMediaId: String?
        ): RemoteMedia? =
            legacyUri(context, obsIdent, localMediaId)?.let { (resolvedMediaId, legacyUri) ->
                LocalMedia(MediaType.JPG, UUID.randomUUID(), ExistingMedia, legacyUri).syncRemote(
                    context, resolvedMediaId, obsIdent
                )
            }

        suspend fun createFromGallery(context: Context, uri: Uri): RemoteMedia {
            val meta = MediaMeta.imported(context, uri)
            return LocalMedia(MediaType.JPG, UUID.randomUUID(), meta, uri)
                .syncRemote(context)
        }
    }
}

@Parcelize
data class EmptyLocalMedia internal constructor(
    val type: MediaType,
    val id: UUID,
    val uri: Uri
) : Parcelable {

    private fun galleryAddPic(context: Context) {
        val file = preQFile(type, id)
        MediaScannerConnection.scanFile(
            context,
            arrayOf(file.toString()),
            arrayOf(type.mime),
            null
        )
    }

    fun externallySuccessfullyCreated(context: Context): LocalMedia {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            galleryAddPic(context)
        }
        openFileDescriptor(context, "r").use {
            val length = it.length
            if (length <= 0) {
                throw IllegalStateException(
                    "Successfully created medium is of non positive size $length"
                )
            }
        }
        val meta = MediaMeta.created(context, uri)
        return LocalMedia(type, id, meta, uri)
    }

    fun externallyFailed(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val file = preQFile(type, id)
            file.delete()
        } else {
            try {
                val resolver = context.contentResolver
                resolver.delete(uri, null, null)
            } catch (e: SecurityException) {
                Log.i(TAG, "Discarded security exception trying to delete failed media", e)
            }
        }
    }

    fun openFileDescriptor(context: Context, mode: String): AssetFileDescriptor =
        context.contentResolver.openAssetFileDescriptor(uri, mode)!!
}

@Parcelize
data class LocalMedia(
    override val type: MediaType,
    override val id: UUID,
    override val meta: MediaMeta,
    val uri: Uri
) : Media {

    override fun availableWithoutPermission(
        context: Context,
        yes: (Uri) -> Unit,
        no: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            yes(uri)
        } else {
            no()
        }
    }

    override suspend fun fetchUri(
        readPermissionGranted: Boolean,
        context: Context
    ): NetworkResult<Uri> =
        NetworkResult.success(uri)

    override suspend fun syncRemote(
        context: Context,
        localMediaId: String?,
        obsIdent: String?
    ): RemoteMedia = withContext(Dispatchers.IO) {
        ObservationDb.getDb(context).operationDao()
            .upsertMedia(context, id, type, uri, uploadFile(context))
        RemoteMedia(type, id, meta, localMediaId, obsIdent)
    }

    override suspend fun upload(
        context: Context
    ): NetworkResult<RemoteMedia> =
        syncRemote(context, null, null).upload(context)
}

@Parcelize
data class RemoteMedia(
    override val type: MediaType,
    override val id: UUID,
    override val meta: MediaMeta,
    val localMediaId: String?,
    val obsIdent: String?
) : Media {
    private fun glideUrl(context: Context): GlideUrl =
        GlideUrl("${BuildConfig.BACKEND_URL}media/$id", glideHeaders(context))

    private fun localUri(context: Context): Uri? {
        val likeFilename = "%${filename(type, id)}%"
        val selectionNewFiles =
            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionOldFiles =
            "${MediaStore.MediaColumns.DATA} LIKE ?"

        val oldAppFilename = localMediaId?.let {
            "%${localMediaIdToFilename(it)}%"
        }

        val (selection, arguments) = if (oldAppFilename != null) {
            Pair(
                """$selectionNewFiles OR $selectionOldFiles OR
                    |$selectionNewFiles OR $selectionOldFiles""".trimMargin(),
                arrayOf(likeFilename, likeFilename, oldAppFilename, oldAppFilename)
            )
        } else {
            Pair(
                "$selectionNewFiles OR $selectionOldFiles",
                arrayOf(likeFilename, likeFilename)
            )
        }

        val uri = context.contentResolver.query(
            afterQContentUri(type),
            arrayOf(MediaStore.MediaColumns._ID),
            selection,
            arguments,
            null
        )?.use { cursor ->
            if (cursor.count > 0) {
                cursor.moveToFirst()
                val id = cursor.getLong(0)
                ContentUris.withAppendedId(afterQContentUri(type), id)
            } else {
                null
            }
        }
        return uri
    }

    private suspend fun downloadWithGlide(context: Context): File =
        withContext(Dispatchers.IO) {
            try {
                Glide
                    .with(context)
                    .download(glideUrl(context))
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
            } catch (e: ExecutionException) {
                val cause = e.cause
                if (cause != null) {
                    throw cause
                } else {
                    throw e
                }
            }
        }

    private fun cachedAudioFile(context: Context): File =
        File("${context.cacheDir}", "recording_$id.${type.extension}")

    private suspend fun remoteUri(context: Context): Uri =
        when (type) {
            MediaType.JPG -> {
                downloadWithGlide(context).toUri()
            }
            MediaType.MP4 -> {
                val file = cachedAudioFile(context)
                if (file.exists()) {
                    file.toUri()
                } else if (obsIdent != null) {
                    val externalFilesDir = context.getExternalFilesDir(null)
                    val local = File(externalFilesDir, "Recordings/$obsIdent.mp4")
                    if (local.exists()) {
                        local.toUri()
                    } else {
                        PublicBackendApi.service.media(id, context, file).toUri()
                    }
                } else {
                    PublicBackendApi.service.media(id, context, file).toUri()
                }
            }
        }

    /** Fetch local URI using READ_EXTERNAL_STORAGE permission
     * localUri function will data from other App instances when used with the
     * READ_EXTERNAL_STORAGE permission.
     */
    private fun fetchUriWithReadExternalPermission(context: Context): Uri? =
        localUri(context)

    override suspend fun fetchUri(
        readPermissionGranted: Boolean,
        context: Context
    ): NetworkResult<Uri> {
        val uploadFile = uploadFile(context)
        val local = if (readPermissionGranted) {
            fetchUriWithReadExternalPermission(context)
        } else {
            null
        }
        return if (local != null) {
            NetworkResult.success(local)
        } else if (uploadFile.exists()) {
            NetworkResult.success(uploadFile.toUri())
        } else {
            catchNetworkAndServerErrors(context) {
                remoteUri(context)
            }
        }
    }

    override suspend fun syncRemote(
        context: Context,
        localMediaId: String?,
        obsIdent: String?
    ): RemoteMedia =
        this

    override fun availableWithoutPermission(
        context: Context,
        yes: (Uri) -> Unit,
        no: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when (val uri = localUri(context)) {
                null -> no()
                else -> yes(uri)
            }
        } else {
            no()
        }
    }

    override suspend fun upload(
        context: Context
    ): NetworkResult<RemoteMedia> {
        val operationDao = ObservationDb.getDb(context).operationDao()
        return if (operationDao.wasUploaded(id)) {
            NetworkResult.success(this)
        } else {
            val copy = uploadFile(context)
            if (copy.exists()) {
                // File was still not uploaded
                catchNetworkAndServerErrors(context) {
                    PublicBackendApi.service.uploadMedia(context, id, copy, type)
                    operationDao.setUploaded(id)
                    this
                }
            } else {
                // File was already uploaded in background and removed by sync
                NetworkResult.success(this)
            }
        }
    }
}

fun Fragment.registerLocalMediaReadPermissionRequest(result: RequestedPermissionsCallback) =
    registerRequestedPermission(
        READ_EXTERNAL_STORAGE,
        result
    )
