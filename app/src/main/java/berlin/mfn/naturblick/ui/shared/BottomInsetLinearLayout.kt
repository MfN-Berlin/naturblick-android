/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import berlin.mfn.naturblick.utils.setupBottomInset

class BottomInsetLinearLayout(context: Context, attributeSet: AttributeSet):
    LinearLayout(context, attributeSet) {
    init {
        setupBottomInset()
    }
}