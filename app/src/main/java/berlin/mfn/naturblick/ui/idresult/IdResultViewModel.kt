/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.idresult

import android.app.Application
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContractBase.Companion.ID_SPECIES
import berlin.mfn.naturblick.utils.IdApi
import berlin.mfn.naturblick.utils.MediaThumbnail
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.RecoverableError
import kotlinx.coroutines.launch

class IdResultViewModel(
    private val identifySpecies: IdentifySpecies,
    savedStateHandle: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {
    private val db = StrapiDb.getDb(application)
    private val speciesDao = db.speciesDao()
    private val currentVersionDao = db.currenVersionDao()
    val thumbnail: MediaThumbnail = identifySpecies.thumbnail

    val isImage =
        identifySpecies is IdentifySpeciesImage

    val isSound =
        identifySpecies is IdentifySpeciesSound

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
                        identifySpecies.media.upload(getApplication()).flatMap { remoteMedia ->
                            identifySpecies.thumbnail.upload(getApplication())
                                .flatMap { remoteThumbnail ->
                                    NetworkResult.catchNetworkAndServerErrors(getApplication()) {
                                        IdApi.service.soundId(
                                            remoteMedia,
                                            remoteThumbnail,
                                            identifySpecies.segmStart,
                                            identifySpecies.segmEnd,
                                            currentVersionDao.getCurrentVersion()
                                        )
                                    }
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val action: IdentifySpecies = savedStateHandle[ID_SPECIES]!!
                IdResultViewModel(action, savedStateHandle, (this[APPLICATION_KEY] as Application))
            }
        }
    }
}

