/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDevice(
    val deviceIdentifier: String,
    val model: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String
) {
    constructor(deviceIdentifier: String, model: String, osVersion: String, appVersion: String) :
        this(deviceIdentifier, model, "android", osVersion, appVersion)
}
