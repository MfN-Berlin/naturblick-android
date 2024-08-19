/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.util.TypedValue
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import berlin.mfn.naturblick.R
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlin.math.roundToInt

@BindingAdapter("adapter")
fun <T, U : RecyclerView.ViewHolder> adapterBinding(
    recyclerView: RecyclerView,
    adapter: ListAdapter<T, U>
) {
    recyclerView.adapter = adapter
}

@BindingAdapter("dividerItemDecoration")
fun dividerItemDecorationBinding(recyclerView: RecyclerView, active: Boolean) {
    if (active) {
        val dividerItemDecoration =
            MaterialDividerItemDecoration(recyclerView.context, RecyclerView.VERTICAL)
        val inset = recyclerView.context.resources.getDimension(R.dimen.avatar_size) +
            recyclerView.context.resources.getDimension(R.dimen.default_margin) * 2
        dividerItemDecoration.dividerInsetStart = inset.roundToInt()
        val value = TypedValue()
        recyclerView.context.theme.resolveAttribute(R.attr.colorOnSecondaryMinimumEmphasis, value, true)
        dividerItemDecoration.dividerColor = value.data
        recyclerView.addItemDecoration(dividerItemDecoration, 0)
    } else {
        recyclerView.removeItemDecorationAt(0)
    }
}
