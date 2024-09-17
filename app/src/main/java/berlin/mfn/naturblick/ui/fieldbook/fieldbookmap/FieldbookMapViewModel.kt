/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbookmap

import android.app.Application
import androidx.lifecycle.*
import berlin.mfn.naturblick.backend.Observation
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.room.StrapiDb
import java.util.*

data class ObservationWithGroup(val observation: Observation, val group: String?)

class FieldbookMapViewModel(occurenceId: UUID?, application: Application) :
    AndroidViewModel(application) {
    private val operationDao = ObservationDb.getDb(application).operationDao()
    private val speciesDao = StrapiDb.getDb(application).speciesDao()
    val observations = liveData {
        val all = operationDao.getMapObservations()
        val mapping = speciesDao.getGroups(all.mapNotNull { it.newSpeciesId }).map {
            Pair(it.id, it.group)
        }.toMap()
        emit(
            all.map { observation ->
                ObservationWithGroup(
                    observation,
                    observation.newSpeciesId?.let {
                        mapping.get(it)
                    }
                )
            }
        )
    }
    val selected = liveData {
        emit(operationDao.getObservation(occurenceId))
    }
    val observationAndSelected = observations.switchMap { observations ->
        selected.map { observation ->
            Pair(observations, observation)
        }
    }
}

class FieldbookMapModelFactory(
    private val occurenceId: UUID?,
    private val application: Application
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FieldbookMapViewModel(
        occurenceId,
        application
    ) as T
}
