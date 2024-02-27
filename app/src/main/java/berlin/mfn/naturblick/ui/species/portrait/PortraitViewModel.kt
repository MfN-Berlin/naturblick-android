package berlin.mfn.naturblick.ui.species.portrait

import androidx.lifecycle.*
import berlin.mfn.naturblick.room.FullPortrait
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.utils.languageId

class PortraitViewModel(
    private val strapiDb: StrapiDb,
    private val speciesId: SpeciesId?,
    private val speciesIdPathFragment: String?
) :
    ViewModel() {

    private suspend fun speciesData(species: Species) = Pair(
        species,
        strapiDb.portraitDao().getPortrait(species.id, languageId())
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
