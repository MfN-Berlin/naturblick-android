package berlin.mfn.naturblick.ui.idresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.ThumbnailRequest
import berlin.mfn.naturblick.databinding.ActivityIdResultBinding
import berlin.mfn.naturblick.ui.BaseActivity
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContract.ID_SPECIES
import berlin.mfn.naturblick.utils.*
import kotlinx.parcelize.Parcelize

sealed interface IdentifySpecies : Parcelable {
    val thumbnail: MediaThumbnail
    val isNew: Boolean
}

@Parcelize
data class IdentifySpeciesImage(
    override val thumbnail: MediaThumbnail,
    override val isNew: Boolean,
    val x: Float,
    val y: Float,
    val size: Float,
    val media: Media
) : IdentifySpecies

@Parcelize
data class IdentifySpeciesSound(
    override val thumbnail: MediaThumbnail,
    override val isNew: Boolean,
    val media: Media,
    val segmStart: Int,
    val segmEnd: Int
) : IdentifySpecies

@Parcelize
data class IdentifySpeciesImageThumbnail(
    override val thumbnail: MediaThumbnail,
    override val isNew: Boolean = false
) : IdentifySpecies

@Parcelize
data class IdentifySpeciesResult(
    val speciesId: Int?,
    val thumbnail: MediaThumbnail
) : Parcelable

object IdResultActivityContract :
    ActivityResultContract<IdentifySpecies, IdentifySpeciesResult?>() {

    override fun createIntent(context: Context, input: IdentifySpecies) =
        Intent(context, IdResultActivity::class.java)
            .putExtra(ID_SPECIES, input)

    override fun parseResult(resultCode: Int, intent: Intent?): IdentifySpeciesResult? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.getParcelableExtra(ID_RESULT)
    }

    const val ID_SPECIES = "id_species"
    const val ID_RESULT = "id_result"
}

class IdResultActivity : BaseActivity(
    R.navigation.id_result_navigation
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val identifySpecies =
            intent.extras?.getParcelable<IdentifySpecies>(
                ID_SPECIES
            )!!
        val viewModel by viewModels<IdResultViewModel> {
            IdResultViewModelFactory(identifySpecies, application)
        }

        val binding = ActivityIdResultBinding.inflate(layoutInflater)
        glideImageUrlThumbnailNoCropBinding(
            binding.image,
            ThumbnailRequest(viewModel.thumbnail, null, null)
        )

        binding.bottomSheet.setUpRootAndTopSheet(
            binding.rootView,
            binding.image
        )

        initializeNavigationViews(binding.root)
    }
}
