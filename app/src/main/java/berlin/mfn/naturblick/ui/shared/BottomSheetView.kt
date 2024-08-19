/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import berlin.mfn.naturblick.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import kotlin.math.roundToInt

class BottomSheetView(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {
    private val margin = resources.getDimension(R.dimen.default_margin)
    private val smallMargin = resources.getDimension(R.dimen.small_margin)

    init {
        orientation = VERTICAL
        setBackgroundResource(R.drawable.bottom_sheet)
        val handle = ImageView(context)
        handle.setImageResource(R.drawable.ic_card_handle)
        handle.layoutParams = LayoutParams(
            (margin * 4).roundToInt(),
            smallMargin.roundToInt()
        ).apply {
            gravity = Gravity.CENTER
            topMargin = margin.roundToInt()
            bottomMargin = margin.roundToInt()
        }

        addView(handle, 0)
    }

    fun setUpRootAndTopSheet(
        root: View,
        topSheet: View
    ) {
        topSheet.viewTreeObserver.addOnGlobalLayoutListener {
            val behavior = BottomSheetBehavior.from(this)
            val padding = margin * 2
            val height = smallMargin
            val offset = Integer.max(
                root.height - topSheet.height,
                (padding + height).roundToInt()
            )
            behavior.setPeekHeight(offset, false)
        }
    }

    fun open() {
        val behavior = BottomSheetBehavior.from(this)
        behavior.state = STATE_EXPANDED
    }

    fun close() {
        val behavior = BottomSheetBehavior.from(this)
        behavior.state = STATE_COLLAPSED
    }
}
