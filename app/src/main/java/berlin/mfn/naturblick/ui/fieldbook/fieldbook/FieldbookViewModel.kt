/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.*
import berlin.mfn.naturblick.backend.DeleteOperation
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.utils.MediaThumbnail
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.languageId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.util.UUID

class FieldbookViewModel(
    occurenceId: UUID?,
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val operationDao = ObservationDb.getDb(application).operationDao()
    private val speciesDao = StrapiDb.getDb(application).speciesDao()
    var launched: Boolean
        get() = savedStateHandle["launched"] ?: false
        set(value) {
            savedStateHandle["launched"] = value
        }

    var query by mutableStateOf("")
       private set

    fun updateQuery(input: String) {
        query = input
    }

    fun deleteObservations(selection: List<UUID>) {
        viewModelScope.launch {
            for (occurenceId in selection.toList()) {
                operationDao.insertOperation(DeleteOperation(occurenceId = occurenceId))
            }
            operationDao.refreshObservations()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val observationsFlow =
        snapshotFlow { query }.flowOn(Dispatchers.IO).flatMapLatest { query ->
            operationDao.getAllObservations().map { observations ->
                Pair(query, observations)
            }
        }.mapLatest { (query, observations) ->
            if (query.isBlank())
                observations
            else {
                val speciesSet = speciesDao.filterSpeciesIds("%$query%", languageId()).toHashSet()
                observations.filter { speciesSet.contains(it.newSpeciesId) }
            }.map {
                if (it.newSpeciesId != null)
                    FieldbookObservation(
                        it.occurenceId,
                        it.created,
                        it.thumbnailId?.let { thumbnailId ->
                            MediaThumbnail.remote(thumbnailId, it.obsIdent)
                        },
                        it.obsIdent,
                        speciesDao.getSpecies(it.newSpeciesId),
                        it.coords
                    )
                else
                    FieldbookObservation(
                        it.occurenceId,
                        it.created,
                        it.thumbnailId?.let { thumbnailId ->
                            MediaThumbnail.remote(thumbnailId, it.obsIdent)
                        },
                        it.obsIdent,
                        null,
                        it.coords
                    )
            }
        }

    var refreshState by mutableStateOf(false)
        private set
    fun refresh() {
        refreshState = true
        viewModelScope.launch {
            NetworkResult.catchNetworkAndServerErrors(getApplication()) {
                PublicBackendApi.service.triggerSync(getApplication())
            }.fold(
                {
                    refreshState = false
                }, { error ->
                    Toast.makeText(
                        getApplication(),
                        error.error,
                        Toast.LENGTH_LONG
                    ).show()
                    refreshState = false
                }
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val observationAndSelected = observationsFlow.mapLatest { observations ->
        val observation = observations.find { it.occurenceId == occurenceId }
        Pair(observations, observation)
    }

    private var startTrackingListener: (() -> Unit)? = null

    fun setStartTrackingListener(listener: () -> Unit) {
        this.startTrackingListener = listener
    }


    private var stopTrackingListener: (() -> Unit)? = null

    fun setStopTrackingListener(listener: () -> Unit) {
        this.stopTrackingListener = listener
    }

    var locationEnabled by mutableStateOf(false)
        private set

    fun startTracking() {
        locationEnabled = true
        startTrackingListener?.let {
            it()
        }
    }

    fun stopTracking() {
        locationEnabled = false
        stopTrackingListener?.let {
            it()
        }
    }
}

class FieldbookViewModelFactory(
    private val occurenceId: UUID?,
    private val application: Application,
) : AbstractSavedStateViewModelFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return FieldbookViewModel(
            occurenceId,
            application,
            handle
        ) as T
    }
}
