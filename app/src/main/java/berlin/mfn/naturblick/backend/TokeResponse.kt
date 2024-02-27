package berlin.mfn.naturblick.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokeResponse(@SerialName("access_token") val accessToken: String)
