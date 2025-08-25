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
        identifySpecies is IdentifySpeciesImage

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

