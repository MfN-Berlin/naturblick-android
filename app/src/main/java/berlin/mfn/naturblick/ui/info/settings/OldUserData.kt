/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.settings

import kotlinx.serialization.Serializable

@Serializable
data class OldUserData(val name: String?, val policy: Boolean?)
