/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.data

import android.content.Context
import android.os.Parcelable
import androidx.databinding.BindingAdapter
import berlin.mfn.naturblick.R
import com.google.android.material.textview.MaterialTextView
import kotlinx.parcelize.Parcelize

sealed interface GroupLabel : Parcelable {
    @Parcelize
    data class Resource(val resId: Int) : GroupLabel

    @Parcelize
    data class Text(val text: String) : GroupLabel
}

fun Context.getLabel(label: GroupLabel): String = when (label) {
    is GroupLabel.Resource -> getString(label.resId)
    is GroupLabel.Text -> label.text
}

@BindingAdapter("groupLabel")
fun MaterialTextView.setGroupLabel(label: GroupLabel?) {
    text = when (label) {
        is GroupLabel.Resource -> context.getString(label.resId)
        is GroupLabel.Text -> label.text
        null -> ""
    }
}

@Parcelize
data class UiGroup(
    val id: String,
    val label: GroupLabel,
    val image: Int
) : Parcelable {
    companion object {

        const val ALL_GROUP_ID = "all"
        const val UNKNOWN_GROUP_ID = "unknown"
        const val OTHERS_GROUP_ID = "others"

        val ALL_GROUP = UiGroup(
            ALL_GROUP_ID,
            GroupLabel.Resource(R.string.all),
            0
        )
        val UNKNOWN_GROUP = UiGroup(
            UNKNOWN_GROUP_ID,
            GroupLabel.Resource(R.string.unknown),
            0
        )
        val OTHERS_GROUP = UiGroup(
            OTHERS_GROUP_ID,
            GroupLabel.Resource(R.string.others),
            0
        )
    }
}