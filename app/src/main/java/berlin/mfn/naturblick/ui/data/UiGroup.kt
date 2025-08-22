/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.data

import android.os.Parcelable
import berlin.mfn.naturblick.utils.isGerman
import kotlinx.parcelize.Parcelize


enum class GroupType {
    FLORA, FAUNA
}

@Parcelize
data class UiGroup(
    val id: String,
    val gername: String,
    val engname: String,
    val image: Int,
    val type: GroupType
) : Parcelable {
    val name
        get() = if (isGerman()) {
            gername
        } else {
            engname
        }
}
