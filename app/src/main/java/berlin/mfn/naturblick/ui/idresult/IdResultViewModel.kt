/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.idresult

import android.app.Application
import androidx.lifecycle.*
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.utils.*
import kotlinx.coroutines.launch

class IdResultViewModel(
    private val identifySpecies: IdentifySpecies,
    savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {
    val db = StrapiDb.getDb(application)
    val speciesDao = db.speciesDao()
    val currentVersionDao = db.currenVersionDao()
    val thumbnail: MediaThumbnail = identifySpecies.thumbnail

    val isImage =
        identifySpecies is IdentifySpeciesImage || identifySpecies is IdentifySpeciesImageThumbnail

    val errorOptions: Int = if (identifySpecies.isNew) {
        R.array.autoid_error_options
    } else {
        R.array.autoid_not_new_error_options
    }

    val infoText: Int = if (isImage) {
        R.string.image_autoid_infotext
    } else {
        R.string.sound_autoid_infotext
    }

    val selectSpeciesItems: Int = if (identifySpecies.isNew) {
        if (isImage) {
            R.array.new_photo_options
        } else {
            R.array.new_sound_options
        }
    } else {
        if (isImage) {
            R.array.photo_options
        } else {
            R.array.sound_options
        }
    }
    val isNew = identifySpecies.isNew

    private val _idResults = savedStateHandle.getLiveData<List<BackendIdResult>>("result")
    private val _recoverableError = savedStateHandle.getLiveData<RecoverableError>("error")
    val recoverableError: LiveData<RecoverableError> = _recoverableError

    fun identify() {
        if (_idResults.value == null) {
            viewModelScope.launch {
                when (identifySpecies) {
                    is IdentifySpeciesImage ->
                        identifySpecies.thumbnail.upload(getApplication()).flatMap { remote ->
                            NetworkResult.catchNetworkAndServerErrors(getApplication()) {
                                IdApi.service.imageId(
                                    remote,
                                    identifySpecies.x,
                                    identifySpecies.y,
                                    identifySpecies.size,
                                    identifySpecies.media,
                                    currentVersionDao.getCurrentVersion()
                                )
                            }
                        }

                    is IdentifySpeciesSound ->
                        identifySpecies.media.upload(getApplication()).flatMap { remote ->
                            NetworkResult.catchNetworkAndServerErrors(getApplication()) {
                                IdApi.service.soundId(
                                    remote,
                                    identifySpecies.segmStart,
                                    identifySpecies.segmEnd,
                                    currentVersionDao.getCurrentVersion()
                                )
                            }
                        }

                    is IdentifySpeciesImageThumbnail -> identifySpecies.thumbnail.upload(
                        getApplication()
                    ).flatMap { remote ->
                        NetworkResult.catchNetworkAndServerErrors(getApplication()) {
                            IdApi.service.imageId(
                                remote,
                                null,
                                null,
                                null,
                                null,
                                currentVersionDao.getCurrentVersion()
                            )
                        }
                    }
                }.fold({ results ->
                    _idResults.value = results
                }, { error ->
                    _recoverableError.value = error
                })
            }
        }
    }

    val idResults: LiveData<List<Pair<Species, BackendIdResult>>> =
        _idResults.switchMap { results ->
            liveData {
                emit(
                    results.map { backendIdResult ->
                        val species = speciesDao.getSpecies(backendIdResult.speciesId)
                        Pair(species, backendIdResult)
                    }
                )
            }
        }

    init {
        if (_idResults.value == null && _recoverableError.value == null) {
            identify()
        }
    }
}

class IdResultViewModelFactory(
    private val identifySpecies: IdentifySpecies,
    private val application: Application
) : AbstractSavedStateViewModelFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return IdResultViewModel(
            identifySpecies, handle, application
        ) as T
    }
}
