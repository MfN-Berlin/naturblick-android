/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CharacterQuery(val number: Int, val query: List<Pair<Int, Float>>) : Parcelable
