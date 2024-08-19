/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook

import android.os.Parcelable
import java.util.*
import kotlinx.parcelize.Parcelize

sealed interface ObservationAction : Parcelable

@Parcelize
data class CreateManualObservation(val speciesId: Int? = null) : ObservationAction

@Parcelize
object CreateImageObservation : ObservationAction

@Parcelize
object CreateImageFromGalleryObservation : ObservationAction

@Parcelize
object CreateAudioObservation : ObservationAction

@Parcelize
data class OpenObservation(val occurenceId: UUID) : ObservationAction
