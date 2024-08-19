/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.ui.species.specieslist.SpeciesListActivity
import kotlinx.parcelize.Parcelize

@Parcelize
data class PickSpeciesResult(val speciesId: Int) : Parcelable

object PickSpecies : ActivityResultContract<Unit, PickSpeciesResult?>() {
    override fun createIntent(context: Context, input: Unit) =
        Intent(context, SpeciesListActivity::class.java)
            .putExtra(SPECIES_SELECTABLE, true)

    override fun parseResult(resultCode: Int, intent: Intent?): PickSpeciesResult? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.let {
            IntentCompat.getParcelableExtra(it, SELECTED_SPECIES, PickSpeciesResult::class.java)
        }
    }
    const val SPECIES_SELECTABLE = "selectable"
    const val SELECTED_SPECIES = "selected_species"
}
