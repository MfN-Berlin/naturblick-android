package berlin.mfn.naturblick.ui.idresult

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class BackendIdResult(
    @SerialName(value = "id") val speciesId: Int,
    @SerialName(value = "score") val score: Double
) : Parcelable
