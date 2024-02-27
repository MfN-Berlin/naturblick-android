package berlin.mfn.naturblick.ui.fieldbook.fieldbookmap

import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class FieldbookMapActivity : BaseActivity(
    R.navigation.fieldbook_map_navigation
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }
}
