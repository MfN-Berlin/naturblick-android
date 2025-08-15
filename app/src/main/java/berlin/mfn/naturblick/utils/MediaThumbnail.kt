/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.backend.UploadThumbnailMediaOperation
import berlin.mfn.naturblick.utils.MediaThumbnail.Companion.thumbnailFile
import berlin.mfn.naturblick.utils.NetworkResult.Companion.catchNetworkAndServerErrors
import berlin.mfn.naturblick.utils.NetworkResult.Companion.success
import java.io.File
import java.util.*
import kotlinx.parcelize.Parcelize

sealed interface MediaThumbnail : Parcelable {
    val id: UUID
    suspend fun upload(context: Context): NetworkResult<RemoteMediaThumbnail>
    suspend fun syncRemote(context: Context): RemoteMediaThumbnail
    companion object {
        fun thumbnailFile(context: Context, id: UUID): File =
            File(context.filesDir, "thumbnail_$id.jpg")

        private fun oldAppThumbnailFile(context: Context, obsIdent: String): File {
            // External files dir does not require permissions since it belongs to App
            val externalFilesDir = context.getExternalFilesDir(null)
            val imageThumbnail = File(externalFilesDir, "Images/${obsIdent}_Avatar.jpg")
            return if (imageThumbnail.exists()) {
                imageThumbnail
            } else {
                File(externalFilesDir, "Recordings/${obsIdent}_Avatar.jpg")
            }
        }

        fun oldFile(obsIdent: String?, context: Context): File? =
            obsIdent?.let {
                val oldFile = oldAppThumbnailFile(context, obsIdent)
                if (oldFile.exists()) {
                    oldFile
                } else {
                    null
                }
            }

        fun createEmpty(context: Context): EmptyLocalMediaThumbnail {
            val id = UUID.randomUUID()
            val file = thumbnailFile(context, id)
            file.createNewFile()
            return EmptyLocalMediaThumbnail(id, file)
        }

        fun remote(id: UUID, obsIdent: String?): RemoteMediaThumbnail {
            return RemoteMediaThumbnail(id, obsIdent)
        }
    }
}

@ConsistentCopyVisibility
@Parcelize
data class EmptyLocalMediaThumbnail internal constructor(val id: UUID, val file: File) :
    Parcelable {
    fun externallySuccessfullyCreated() = LocalMediaThumbnail(id, file)
    fun delete() {
        file.delete()
    }

    val uri: Uri get() = file.toUri()
}

@Parcelize
data class LocalMediaThumbnail(override val id: UUID, val file: File) : MediaThumbnail {
    fun delete() {
        file.delete()
    }

    val uri: Uri get() = file.toUri()

    override suspend fun syncRemote(context: Context): RemoteMediaThumbnail {
        ObservationDb.getDb(context).operationDao().upsertThumbnail(
            UploadThumbnailMediaOperation(
                mediaId = id,
                filename = file.name
            )
        )
        return RemoteMediaThumbnail(id, null)
    }

    override suspend fun upload(context: Context): NetworkResult<RemoteMediaThumbnail> =
        catchNetworkAndServerErrors(context) {
            PublicBackendApi.service.uploadMedia(context, id, file, MediaType.JPG)
            RemoteMediaThumbnail(id, null)
        }
}

@Parcelize
data class RemoteMediaThumbnail(override val id: UUID, val obsIdent: String?) : MediaThumbnail {
    fun localFile(context: Context): File? {
        val file = thumbnailFile(context, id)
        return if (file.exists())
            file
        else
            null
    }

    override suspend fun upload(context: Context): NetworkResult<RemoteMediaThumbnail> =
        localFile(context)?.let { file ->
            catchNetworkAndServerErrors(context) {
                PublicBackendApi.service.uploadMedia(context, id, file, MediaType.JPG)
                RemoteMediaThumbnail(id, obsIdent)
            }
        } ?: success(RemoteMediaThumbnail(id, obsIdent)) // If we do not have it, the server has it

    override suspend fun syncRemote(context: Context): RemoteMediaThumbnail =
        this
}
