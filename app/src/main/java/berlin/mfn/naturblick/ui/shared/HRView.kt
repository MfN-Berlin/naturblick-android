/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import berlin.mfn.naturblick.R
import kotlin.math.roundToInt

class HRView(context: Context, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    init {
        val value = TypedValue()
        context.theme.resolveAttribute(R.attr.colorOnSecondaryMinimumEmphasis, value, true)
        setBackgroundColor(value.data)
    }
    constructor(context: Context) : this(context, null) {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            resources.getDimension(R.dimen.hr_height).roundToInt()
        )
    }
}
