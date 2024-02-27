package berlin.mfn.naturblick.backend

import kotlinx.serialization.Serializable

@Serializable
data class SyncInfo(
    val deviceIdentifier: String,
    val syncId: Long?
)

@Serializable
data class ObservationOperationsRequest(
    val operations: List<ObservationOperation>,
    val syncInfo: SyncInfo
)
