/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.portrait

import androidx.lifecycle.*
import berlin.mfn.naturblick.room.FullPortrait
import berlin.mfn.naturblick.room.FullSimilarSpecies
import berlin.mfn.naturblick.room.ImageWithSizes
import berlin.mfn.naturblick.room.SimilarSpecies
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.utils.languageId

class PortraitViewModel(
    private val strapiDb: StrapiDb,
    private val speciesId: SpeciesId?,
    private val speciesIdPathFragment: String?
) :
    ViewModel() {

    private suspend fun getFullSimilarSpecies(similarSpecies: SimilarSpecies): FullSimilarSpecies {
        return FullSimilarSpecies(
            strapiDb.speciesDao().getSpecies(similarSpecies.similarToId),
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
                    speciesData(strapiDb.speciesDao().getSpecies(speciesIdPathFragment.toInt()))
                } catch (_: java.lang.NumberFormatException) {
                    speciesData(strapiDb.speciesDao().getSpecies(speciesIdPathFragment))
                }
            } else {
                speciesData(strapiDb.speciesDao().getSpecies(speciesId?.value!!))
            }
        )
    }
}

class PortraitViewModelFactory(
    private val strapiDb: StrapiDb,
    private val speciesId: SpeciesId?,
    private val speciesIdPathFragment: String?
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PortraitViewModel(strapiDb, speciesId, speciesIdPathFragment) as T
}
