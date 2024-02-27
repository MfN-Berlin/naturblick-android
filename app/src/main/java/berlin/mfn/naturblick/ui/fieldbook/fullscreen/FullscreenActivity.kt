package berlin.mfn.naturblick.ui.fieldbook.fullscreen

import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class FullscreenActivity : BaseActivity(R.navigation.fullscreen_navigation) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }
}
