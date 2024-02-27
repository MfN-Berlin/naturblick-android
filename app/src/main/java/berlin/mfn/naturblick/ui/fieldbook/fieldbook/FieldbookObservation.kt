package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.net.Uri
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.backend.ThumbnailRequest
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.utils.MediaThumbnail
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

data class FieldbookObservation(
    val occurenceId: UUID,
    val created: ZonedDateTime,
    val thumbnail: MediaThumbnail?,
    val obsIdent: String?,
    val species: Species?
) {
    private val speciesUri: Uri?
        get() = if (species?.imageUrl != null)
            Uri.parse(BuildConfig.STRAPI_URL + species.imageUrl)
        else
            null

    val thumbnailRequest: ThumbnailRequest
        get() =
            ThumbnailRequest(thumbnail, speciesUri, obsIdent)

    val name = species?.name

    val nameOrSciname = species?.name ?: species?.sciname

    val localDateTimeString: String get() = FORMAT.format(created)

    companion object {
        val FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    }
}
