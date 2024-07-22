package berlin.mfn.naturblick.ui.sound

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import berlin.mfn.naturblick.utils.Media
import berlin.mfn.naturblick.utils.MediaThumbnail
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropAndIdentifySoundResult(
    val speciesId: Int?,
    val media: Media,
    val thumbnail: MediaThumbnail?
) : Parcelable

@Parcelize
data class CropAndIdentifySoundRequest(val media: Media, val segmStart: Int?, val segmEnd: Int?) :
    Parcelable

object CropAndIdentifySound :
    ActivityResultContract<CropAndIdentifySoundRequest?, CropAndIdentifySoundResult?>() {
    override fun createIntent(context: Context, media: CropAndIdentifySoundRequest?) =
        Intent(context, SoundIdActivity::class.java)
            .putExtra(MEDIA, media)

    override fun parseResult(resultCode: Int, result: Intent?): CropAndIdentifySoundResult? {
        return if (resultCode != Activity.RESULT_OK) {
            null
        } else {
            result?.getParcelableExtra(CROP_AND_IDENTIFY_SOUND_RESULT)
        }
    }

    const val MEDIA = "media"
    const val CROP_AND_IDENTIFY_SOUND_RESULT = "crop_and_identify_sound_result"
}
