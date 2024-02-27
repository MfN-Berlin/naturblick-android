package berlin.mfn.naturblick.ui.info.feedback

import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class FeedbackActivity : BaseActivity(R.navigation.feedback_navigation) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }
}
