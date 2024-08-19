/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import android.content.Context
import android.net.Uri
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.utils.*
import java.io.File

sealed class Thumbnail

data class AuthRemoteThumbnail(val url: String) : Thumbnail()
data class RemoteThumbnail(val url: String) : Thumbnail()
data class FileThumbnail(val file: File) : Thumbnail()
data class ResourceThumbnail(val resourceId: Int) : Thumbnail()

data class ThumbnailRequest(
    val mediaThumbnail: MediaThumbnail?,
    val fallback: Uri?,
    val obsIdent: String?
) {

    fun thumbnail(context: Context): Thumbnail =
        when (mediaThumbnail) {
            is RemoteMediaThumbnail -> {
                val file = mediaThumbnail.localFile(context)
                if (file != null) {
                    FileThumbnail(file)
                } else {
                    val path = "media/${mediaThumbnail.id}"
                    AuthRemoteThumbnail(
                        "${BuildConfig.BACKEND_URL}$path"
                    )
                }
            }
            is LocalMediaThumbnail -> FileThumbnail(mediaThumbnail.file)
            null ->
                obsIdent?.let {
                    if (!it.startsWith("Man")) {
                        MediaThumbnail.oldFile(it, context)?.let { oldThumbnail ->
                            FileThumbnail(oldThumbnail)
                        }
                    } else {
                        null
                    }
                } ?: if (fallback != null) {
                    RemoteThumbnail(fallback.toString())
                } else {
                    // Placeholder if no valid thumbnail can be constructed
                    ResourceThumbnail(R.drawable.placeholder)
                }
        }
}
