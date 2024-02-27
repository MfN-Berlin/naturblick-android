package berlin.mfn.naturblick.ui.species

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CharacterQuery(val number: Int, val query: List<Pair<Int, Float>>) : Parcelable
