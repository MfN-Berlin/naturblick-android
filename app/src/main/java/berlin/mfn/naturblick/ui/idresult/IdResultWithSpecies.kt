/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.idresult

import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.ui.species.portrait.SpeciesId
import kotlin.math.roundToInt

typealias SpeciesAgreeClick = (SpeciesId) -> Unit

data class IdResultWithSpecies(
    val species: Species,
    val score: Double,
    val agreeClick: SpeciesAgreeClick
) {
    val scoreInt: Int = score.roundToInt()
}
