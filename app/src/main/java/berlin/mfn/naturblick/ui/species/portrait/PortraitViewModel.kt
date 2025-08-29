/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.portrait

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import berlin.mfn.naturblick.room.FullPortrait
import berlin.mfn.naturblick.room.FullSimilarSpecies
import berlin.mfn.naturblick.room.ImageWithSizes
import berlin.mfn.naturblick.room.SimilarSpecies
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.sound.CropSoundFragment
import berlin.mfn.naturblick.utils.languageId

class PortraitViewModel(
    application: Application,
    val speciesId: SpeciesId?,
    private val speciesIdPathFragment: String?,
    val allowSelection: Boolean
) :
    ViewModel() {

    private val strapiDb: StrapiDb = StrapiDb.getDb(application)

    private suspend fun getFullSimilarSpecies(similarSpecies: SimilarSpecies): FullSimilarSpecies {
        return FullSimilarSpecies(
            strapiDb.speciesDao().getAcceptedSpecies(similarSpecies.similarToId),
            similarSpecies.portraitId,
            similarSpecies.differences
        )
    }

    private suspend fun getImageWithSize(portraitImageId: Int): ImageWithSizes? {
        val dao = strapiDb.portraitDao()
        return dao.getPortraitImage(portraitImageId)?.let {
            ImageWithSizes(it, dao.getPortraitImageSizes(portraitImageId))
        }
    }

    private suspend fun getFullPortrait(species: Species): FullPortrait? {
        val dao = strapiDb.portraitDao()
        return dao.getPortrait(species.id, languageId())?.let { portrait ->
            FullPortrait(
                portrait,
                portrait.descriptionImageId?.let {
                    getImageWithSize(it)
                },
                portrait.inTheCityImageId?.let {
                    getImageWithSize(it)
                },
                portrait.goodToKnowImageId?.let {
                    getImageWithSize(it)
                },
                dao.getSimilarSpecies(portrait.id).map {
                    getFullSimilarSpecies(it)
                },
                dao.getUnambiguousFeatures(portrait.id),
                dao.getGoodToKnows(portrait.id)
            )
        }
    }

    private suspend fun speciesData(species: Species) = Pair(
        species,
        getFullPortrait(species)
    )

    val speciesAndPortrait: LiveData<Pair<Species, FullPortrait?>> = liveData {
        emit(
            if (speciesIdPathFragment != null) {
                try {
                    speciesData(strapiDb.speciesDao().getAcceptedSpecies(speciesIdPathFragment.toInt()))
                } catch (_: java.lang.NumberFormatException) {
                    speciesData(strapiDb.speciesDao().getSpecies(speciesIdPathFragment))
                }
            } else {
                speciesData(strapiDb.speciesDao().getAcceptedSpecies(speciesId?.value!!))
            }
        )
    }
    companion object {
        val SPECIES_ID_PATH_FRAGMENT_KEY = object : CreationExtras.Key<String?> {}
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val args = PortraitFragmentArgs.fromSavedStateHandle(savedStateHandle)
                val speciesIdPathFragment: String? = this[SPECIES_ID_PATH_FRAGMENT_KEY]

                PortraitViewModel(
                    (this[APPLICATION_KEY] as Application),
                    args.speciesId,
                    speciesIdPathFragment,
                    args.allowSelection
                )
            }
        }
    }
}
