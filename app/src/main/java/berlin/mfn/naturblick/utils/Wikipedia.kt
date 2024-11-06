/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.net.Uri
import berlin.mfn.naturblick.room.Species

object Wikipedia {
    private const val deBaseUrl = "https://de.wikipedia.org"
    private const val enBaseUrl = "https://en.wikipedia.org"

    private fun wikiName(sciname: String): String? {
        val tokens = sciname.split(" ").take(2)
        return if(tokens[0].lowercase() == "Gattung".lowercase()) {
            null
        } else {
            Uri.encode(tokens.joinToString("_"))
        }
    }

    fun uri(species: Species): Uri? {
        val wikiName = wikiName(species.sciname)
        return if (isGerman()) {
            species.wikipedia?.let {
                Uri.parse(it)
            } ?: wikiName?.let { Uri.parse("$deBaseUrl/wiki/$it") }
        } else {
            wikiName?.let { Uri.parse("$enBaseUrl/wiki/$it") }
        }
    }

}