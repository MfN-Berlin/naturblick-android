package berlin.mfn.naturblick.backend

import kotlinx.serialization.Serializable

@Serializable
data class ObservationResponse(
    val data: List<BackendObservation>,
    val partial: Boolean,
    val syncId: Long?
)
