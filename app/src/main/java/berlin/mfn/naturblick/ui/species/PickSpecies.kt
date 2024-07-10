package berlin.mfn.naturblick.ui.species

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
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
        return intent?.getParcelableExtra(SELECTED_SPECIES)
    }
    const val SPECIES_SELECTABLE = "selectable"
    const val SELECTED_SPECIES = "selected_species"
}
