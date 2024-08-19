/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.observation

import berlin.mfn.naturblick.backend.ObsType
import berlin.mfn.naturblick.backend.Observation
import java.time.ZonedDateTime
import java.util.*
import junit.framework.Assert.assertEquals
import org.junit.Test

class ObservationUpdateTest {
    private val occurenceId = UUID.randomUUID()
    private val now = ZonedDateTime.now()
    private val obsIdent = "TEST"
    private val observation =
        Observation(
            occurenceId,
            obsIdent,
            ObsType.MANUAL,
            null,
            now,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
    private val initialObservation = ObservationUpdate(observation)

    @Test
    fun merge_details_update_with_observation_with_details_keeps_update() {
        val observationWithDetails = observation.copy(details = "original")
        val initial = ObservationUpdate(observationWithDetails)
        val merge = initial.merge(ObservationUpdate(details = "update"))
        assertEquals(merge, initial.copy(details = "update"))
    }

    @Test
    fun merge_observation_with_observation_with_details_then_with_details_update_keeps_update() {
        val observationWithDetails = observation.copy(details = "original")
        val observationUpdate = ObservationUpdate(observationWithDetails)
        val merge =
            initialObservation.merge(observationUpdate).merge(ObservationUpdate(details = "update"))
        assertEquals(merge, observationUpdate.copy(details = "update"))
    }

    @Test
    fun merge_details_update_with_observation_then_observation_with_details_keeps_update() {
        val observationWithDetails = observation.copy(details = "original")
        val observationUpdate = ObservationUpdate(observationWithDetails)
        val merge =
            ObservationUpdate(details = "update").merge(initialObservation).merge(observationUpdate)
        assertEquals(observationUpdate.copy(details = "update"), merge)
    }

    @Test
    fun merge_details_update_with_observation_with_the_same_details_reset_update() {
        val observationWithDetails = observation.copy(details = "original")
        val initial = ObservationUpdate(observationWithDetails)
        val merge =
            ObservationUpdate(details = "original").merge(initial)
        assertEquals(initial, merge)
    }

    @Test
    fun merge_observation_with_the_same_details_with_details_update_reset_update() {
        val observationWithDetails = observation.copy(details = "original")
        val initial = ObservationUpdate(observationWithDetails)
        val merge =
            initial.merge(ObservationUpdate(details = "original"))
        assertEquals(initial, merge)
    }
}
