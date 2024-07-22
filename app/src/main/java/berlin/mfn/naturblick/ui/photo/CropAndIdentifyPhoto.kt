package berlin.mfn.naturblick.ui.photo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
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
    override fun createIntent(context: Context, request: CropAndIdentifyPhotoRequest) =
        Intent(context, ImageIdActivity::class.java)
            .putExtra(CROP_AND_IDENTIFY_REQUEST, request)

    override fun parseResult(resultCode: Int, result: Intent?): CropAndIdentifyPhotoResult? {
        return if (resultCode != Activity.RESULT_OK) {
            null
        } else {
            result?.getParcelableExtra(CROP_AND_IDENTIFY_RESULT)
        }
    }

    const val CROP_AND_IDENTIFY_REQUEST = "crop_and_identify_request"
    const val CROP_AND_IDENTIFY_RESULT = "crop_and_identify_result"
}
