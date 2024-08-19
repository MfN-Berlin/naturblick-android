/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TokeResponse(@SerialName("access_token") val accessToken: String)
