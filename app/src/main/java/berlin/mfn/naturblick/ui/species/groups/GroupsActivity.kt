package berlin.mfn.naturblick.ui.species.groups

import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class GroupsActivity : BaseActivity(
    R.navigation.groups_navigation
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }
}
