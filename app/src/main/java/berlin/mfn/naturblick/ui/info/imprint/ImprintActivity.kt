package berlin.mfn.naturblick.ui.info.imprint

import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class ImprintActivity : BaseActivity(R.navigation.imprint_navigation) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }
}
