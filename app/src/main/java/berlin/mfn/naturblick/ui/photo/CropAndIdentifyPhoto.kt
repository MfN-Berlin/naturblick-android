/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.photo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.utils.Media
import berlin.mfn.naturblick.utils.MediaThumbnail
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropAndIdentifyPhotoResult(
    val speciesId: Int?,
    val media: Media,
    val thumbnail: MediaThumbnail?
) : Parcelable

sealed interface CropAndIdentifyPhotoRequest : Parcelable

@Parcelize
object CropAndIdentifyNewPhotoRequest : CropAndIdentifyPhotoRequest

@Parcelize
object CropAndIdentifyGalleryRequest : CropAndIdentifyPhotoRequest

@Parcelize
data class CropAndIdentifyMediaRequest(val media: Media) : CropAndIdentifyPhotoRequest

object CropAndIdentifyPhoto :
    ActivityResultContract<CropAndIdentifyPhotoRequest, CropAndIdentifyPhotoResult?>() {
    override fun createIntent(context: Context, input: CropAndIdentifyPhotoRequest) =
        Intent(context, ImageIdActivity::class.java)
            .putExtra(CROP_AND_IDENTIFY_REQUEST, input)

    override fun parseResult(resultCode: Int, intent: Intent?): CropAndIdentifyPhotoResult? {
        return if (resultCode != Activity.RESULT_OK) {
            null
        } else {
            intent?.let {
                IntentCompat.getParcelableExtra(
                    it,
                    CROP_AND_IDENTIFY_RESULT,
                    CropAndIdentifyPhotoResult::class.java
                )
            }
        }
    }

    const val CROP_AND_IDENTIFY_REQUEST = "crop_and_identify_request"
    const val CROP_AND_IDENTIFY_RESULT = "crop_and_identify_result"
}
