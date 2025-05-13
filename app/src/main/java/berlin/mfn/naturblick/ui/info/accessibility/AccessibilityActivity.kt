/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.accessibility

import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class AccessibilityActivity : BaseActivity(R.navigation.accessibility_navigation) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }
}
