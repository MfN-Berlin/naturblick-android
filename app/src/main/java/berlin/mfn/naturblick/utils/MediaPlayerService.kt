/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

object MediaPlayerService {
    fun timeFormat(time: Float): String {
        return String.format("%.1fs", time)
    }
}
