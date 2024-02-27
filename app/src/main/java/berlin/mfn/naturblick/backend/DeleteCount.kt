package berlin.mfn.naturblick.backend

import kotlinx.serialization.Serializable

@Serializable
data class DeleteCount(val numDetachedObservations: Int)
