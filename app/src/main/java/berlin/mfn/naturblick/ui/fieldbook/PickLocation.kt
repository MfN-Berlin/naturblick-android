package berlin.mfn.naturblick.ui.fieldbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import berlin.mfn.naturblick.backend.Coordinates
import berlin.mfn.naturblick.ui.fieldbook.locationpicker.LocationPickerActivity
import berlin.mfn.naturblick.ui.fieldbook.locationpicker.LocationPickerFragment.Companion.INITIAL_LOCATION
import berlin.mfn.naturblick.ui.fieldbook.locationpicker.LocationPickerFragment.Companion.PICKED_LOCATION

object PickLocation : ActivityResultContract<Coordinates?, Coordinates?>() {
    override fun createIntent(context: Context, c: Coordinates?) =
        Intent(context, LocationPickerActivity::class.java)
            .putExtra(INITIAL_LOCATION, c)

    override fun parseResult(resultCode: Int, result: Intent?): Coordinates? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return result?.getParcelableExtra(PICKED_LOCATION)
    }
}
