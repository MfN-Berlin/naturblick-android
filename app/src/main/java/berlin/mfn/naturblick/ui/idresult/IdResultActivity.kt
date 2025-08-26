/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.idresult

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.ThumbnailRequest
import berlin.mfn.naturblick.databinding.ActivityIdResultBinding
import berlin.mfn.naturblick.ui.BaseActivity
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContractBase.Companion.ID_SPECIES
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
data class IdentifySpeciesResult(
    val speciesId: Int?,
    val thumbnail: MediaThumbnail
) : Parcelable


abstract class IdResultActivityContractBase<T>:
    ActivityResultContract<IdentifySpecies, T>() {

    override fun createIntent(context: Context, input: IdentifySpecies) =
        Intent(context, IdResultActivity::class.java)
            .putExtra(ID_SPECIES, input)

    companion object {
        const val ID_SPECIES = "id_species"
        const val ID_RESULT = "id_result"
        const val RESULT_DISCARD = Activity.RESULT_FIRST_USER
    }
}

object IdResultActivityContract : IdResultActivityContractBase<IdentifySpeciesResult?>() {

    override fun parseResult(resultCode: Int, intent: Intent?): IdentifySpeciesResult? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        return intent?.let {
            IntentCompat.getParcelableExtra(it, ID_RESULT, IdentifySpeciesResult::class.java)
        }
    }
}

enum class Result {
    OK, RETRY, DISCARD
}

object CancelableIdResultActivityContract : IdResultActivityContractBase<Pair<Result, IdentifySpeciesResult?>>() {

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<Result, IdentifySpeciesResult?> {
        if (resultCode == Activity.RESULT_OK) {
            return Pair(Result.OK, intent?.let {
                IntentCompat.getParcelableExtra(it, ID_RESULT, IdentifySpeciesResult::class.java)
            })
        } else if (resultCode == Companion.RESULT_DISCARD) {
            return Pair(Result.DISCARD, null)
        } else {
            return Pair(Result.RETRY, null)
        }

    }
}

class IdResultActivity : BaseActivity(
    R.navigation.id_result_navigation
) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val identifySpecies =
            IntentCompat.getParcelableExtra(intent, ID_SPECIES, IdentifySpecies::class.java)!!
        val viewModel by viewModels<IdResultViewModel> {
            viewModelFactory {
                initializer {
                    IdResultViewModel(identifySpecies, createSavedStateHandle(), application)
                }
            }
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
