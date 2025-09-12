/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import berlin.mfn.naturblick.backend.DeleteOperation
import berlin.mfn.naturblick.backend.Observation
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.data.GroupRepo
import berlin.mfn.naturblick.ui.data.UiGroup
import berlin.mfn.naturblick.utils.MediaThumbnail
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.languageId
import com.mapbox.maps.CameraState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.util.UUID

class FieldbookViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val operationDao = ObservationDb.getDb(application).operationDao()
    private val speciesDao = StrapiDb.getDb(application).speciesDao()

    private suspend fun toFieldbookObservation(observation: Observation): FieldbookObservation =
        FieldbookObservation(
            observation.occurenceId,
            observation.created,
            observation.thumbnailId?.let { thumbnailId ->
                MediaThumbnail.remote(thumbnailId, observation.obsIdent)
            },
            observation.obsIdent,
            observation.newSpeciesId?.let { speciesDao.getAcceptedSpecies(it) },
            observation.coords
        )

    private var observationSelected: ((Pair<FieldbookObservation, Boolean>?) -> Unit)? = null
    fun setObservationSelectedListener(observationSelected: (Pair<FieldbookObservation, Boolean>?) -> Unit) {
        this.observationSelected = observationSelected
    }

    fun selectObservation(occurenceId: UUID?, moveTo: Boolean = false) {
        viewModelScope.launch {
            observationSelected?.invoke(occurenceId?.let {
                Pair(toFieldbookObservation(operationDao.getObservation(it)), moveTo)
            })
        }
    }

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

    var group: UiGroup by mutableStateOf(UiGroup.Companion.ALL_GROUP)
        private set

    fun updateGroup(group: UiGroup) {
        this.group = group
    }

    fun deleteObservations(selection: List<UUID>) {
        viewModelScope.launch {
            for (occurenceId in selection.toList()) {
                operationDao.insertOperation(DeleteOperation(occurenceId = occurenceId))
            }
            operationDao.refreshObservations()
        }
    }

    val queryFlow = snapshotFlow { query }.flowOn(Dispatchers.IO)
    val groupFlow = snapshotFlow { group }.flowOn(Dispatchers.IO)

    @OptIn(ExperimentalCoroutinesApi::class)
    val observationsFlow =
        queryFlow.combine(groupFlow) { query, group ->
            Pair(query, group)
        }.flatMapLatest { queryAndGroup ->
            operationDao.getAllObservations().map { observations ->
                Pair(queryAndGroup, observations)
            }
        }.mapLatest { (queryAndGroup, observations) ->
            val (query, group) = queryAndGroup
            if (group == UiGroup.Companion.UNKNOWN_GROUP) {
                observations.filter {
                    it.newSpeciesId == null
                }.map { toFieldbookObservation(it) }
            } else {
                val speciesSet = when (group) {
                    UiGroup.Companion.OTHERS_GROUP -> speciesDao.filterOthersSpeciesIds(
                        "%$query%",
                        languageId()
                    ).toHashSet()

                    UiGroup.Companion.ALL_GROUP -> speciesDao.filterSpeciesIds(
                        "%$query%",
                        null,
                        languageId()
                    )
                        .toHashSet()

                    else -> speciesDao.filterSpeciesIds("%$query%", group.id, languageId())
                        .toHashSet()
                }

                observations.filter {
                    speciesSet.contains(it.newSpeciesId) || (it.newSpeciesId == null
                            && group == UiGroup.Companion.ALL_GROUP && query.isEmpty())
                }
                    .map { toFieldbookObservation(it) }
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    val selectableGroupsFlow =
        operationDao.getAllObservations().mapLatest { obervations ->
            val obsGroups = obervations
                .map { toFieldbookObservation(it) }
                .mapNotNull { it.species?.group }
                .distinct()

            val groups = GroupRepo.getFieldbookFilterGroups(application, obsGroups)

            val withUnknown = obervations.any { it.newSpeciesId == null }
            val withOthers = !groups.map { it.id }.containsAll(obsGroups)

            val selectableGroups = buildList {
                add(UiGroup.Companion.ALL_GROUP)
                if (withUnknown) add(UiGroup.Companion.UNKNOWN_GROUP)
                addAll(groups)
                if (withOthers) add(UiGroup.Companion.OTHERS_GROUP)
            }

            // reset filter if it is set and user deletes obs
            if (selectableGroups.size <= SHOW_FILTER_THRESHOLD) {
                updateGroup(UiGroup.Companion.ALL_GROUP)
            }

            selectableGroups
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

    var cameraState: CameraState? = null
        private set

    fun setCameraState(cameraState: CameraState) {
        this.cameraState = cameraState
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                FieldbookViewModel((this[APPLICATION_KEY] as Application), savedStateHandle)
            }
        }
    }
}
