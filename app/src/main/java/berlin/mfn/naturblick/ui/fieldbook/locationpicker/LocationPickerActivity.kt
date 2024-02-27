package berlin.mfn.naturblick.ui.fieldbook.locationpicker

import android.app.Activity
import android.os.Bundle
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class LocationPickerActivity : BaseActivity(
    R.navigation.locationpicker_navigation
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        finish()
        return true
    }
}
