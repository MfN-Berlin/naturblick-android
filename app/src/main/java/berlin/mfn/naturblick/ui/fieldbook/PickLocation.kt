/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.backend.Coordinates
import berlin.mfn.naturblick.ui.fieldbook.locationpicker.LocationPickerActivity

object PickLocation : ActivityResultContract<Coordinates?, Coordinates?>() {
    override fun createIntent(context: Context, input: Coordinates?) =
        Intent(context, LocationPickerActivity::class.java)
            .putExtra(LocationPickerActivity.INITIAL_LOCATION, input)

    override fun parseResult(resultCode: Int, intent: Intent?): Coordinates? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.let {
            IntentCompat.getParcelableExtra(it,LocationPickerActivity.PICKED_LOCATION, Coordinates::class.java)
        }
    }
}
