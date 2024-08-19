/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.observation

import berlin.mfn.naturblick.backend.*
import java.time.ZonedDateTime
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class EditObservationFlowTest {
    private val occurenceId = UUID.randomUUID()
    private val now = ZonedDateTime.now()
    private val obsIdent = "TEST"
    private val observation =
        Observation(
            occurenceId, obsIdent, ObsType.MANUAL, null, now, null, null,
            null, null, null, null, null
        )
    private val initialObservation = ObservationUpdate(observation)

    @Test
    fun empty_update_leaves_initial_observation_unchanged() {
        runBlocking {
            val initial = flowOf(initialObservation)
            val updates = flow<ObservationUpdate> {}
            val flow = ObservationViewModel.updateFlow(initial, updates)
            assertEquals(listOf(initialObservation), flow.toList())
        }
    }

    @Test
    fun second_observation_is_the_last() {
        runBlocking {
            val second =
                initialObservation.copy(
                    observation = observation.copy(details = "New details from backend")
                )
            val initial = flowOf(initialObservation, second)
            val updates = flow<ObservationUpdate> {}
            val flow = ObservationViewModel.updateFlow(initial, updates)
            assertEquals(listOf(initialObservation, second), flow.toList())
        }
    }

    @Test
    fun update_details_changes_initial_observation() {
        runBlocking {
            val initial = flowOf(initialObservation)
            val updates = flowOf(ObservationUpdate(details = "Details changed"))
            val flow = ObservationViewModel.updateFlow(initial, updates)
            assertEquals(
                listOf(
                    initialObservation,
                    initialObservation.copy(details = "Details changed")
                ),
                flow.toList()
            )
        }
    }

    @Test
    fun update_details_changes_also_second_observation() {
        runBlocking {
            val second =
                initialObservation.copy(
                    observation = observation.copy(details = "New details from backend")
                )
            val initial = flow {
                emit(initialObservation)
                delay(100)
                emit(second)
            }
            val updates = flow {
                emit(ObservationUpdate(details = "Details changed"))
            }
            val flow = ObservationViewModel.updateFlow(initial, updates)
            assertEquals(
                listOf(
                    initialObservation,
                    initialObservation.copy(details = "Details changed"),
                    second.copy(details = "Details changed")
                ),
                flow.toList()
            )
        }
    }

    @Test
    fun update_details_twice_last_change_is_last_state() {
        runBlocking {
            val initial = flowOf(initialObservation)
            val updates = flowOf(
                ObservationUpdate(details = "Details changed"),
                ObservationUpdate(details = "Details changed again")
            )
            val flow = ObservationViewModel.updateFlow(initial, updates)
            assertEquals(
                listOf(
                    initialObservation,
                    initialObservation.copy(details = "Details changed"),
                    initialObservation.copy(details = "Details changed again")
                ),
                flow.toList()
            )
        }
    }

    @Test
    fun update_details_and_individuals_both_updates_observation() {
        runBlocking {
            val initial = flowOf(initialObservation)
            val updates = flowOf(
                ObservationUpdate(details = "Details changed"),
                ObservationUpdate(individuals = 2)
            )
            val flow = ObservationViewModel.updateFlow(initial, updates)
            assertEquals(
                listOf(
                    initialObservation,
                    initialObservation.copy(details = "Details changed"),
                    initialObservation.copy(details = "Details changed", individuals = 2)
                ),
                flow.toList()
            )
        }
    }
}
