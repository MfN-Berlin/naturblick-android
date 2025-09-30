/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.ui.species.PickFaunaSpecies.SPECIES_SELECTABLE
import berlin.mfn.naturblick.ui.species.PickSpecies.SELECTED_SPECIES
import berlin.mfn.naturblick.ui.species.portrait.PortraitActivity
import berlin.mfn.naturblick.ui.species.portrait.SpeciesId

object ConfirmSpecies : ActivityResultContract<SpeciesId, PickSpeciesResult?>() {
    const val SPECIES_ID = "species_id"
    override fun createIntent(
        context: Context,
        input: SpeciesId
    ): Intent =
        Intent(context, PortraitActivity::class.java)
            .putExtra(SPECIES_SELECTABLE, true)
            .putExtra(SPECIES_ID, input)

    override fun parseResult(resultCode: Int, intent: Intent?): PickSpeciesResult? =
        if(resultCode == Activity.RESULT_OK) {
            intent?.let {
                IntentCompat.getParcelableExtra(it, SELECTED_SPECIES, PickSpeciesResult::class.java)
            }
        } else {
            null
        }

}