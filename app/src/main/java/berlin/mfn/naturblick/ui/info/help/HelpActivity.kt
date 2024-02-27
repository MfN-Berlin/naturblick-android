package berlin.mfn.naturblick.ui.info.help

import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class HelpActivity : BaseActivity(R.navigation.help_navigation) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }
}
