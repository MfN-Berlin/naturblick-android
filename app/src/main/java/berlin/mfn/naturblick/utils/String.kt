/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

fun String.toSQLLikeQuery(): String? =
    if(this.isEmpty()) {
        null
    } else {
        "%$this%"
    }