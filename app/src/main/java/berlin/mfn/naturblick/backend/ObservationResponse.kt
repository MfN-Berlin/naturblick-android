/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import kotlinx.serialization.Serializable

@Serializable
data class ObservationResponse(
    val data: List<BackendObservation>,
    val partial: Boolean,
    val syncId: Long?
)
