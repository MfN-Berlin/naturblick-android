/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.ui.fieldbook.observation.ObservationActivity
import java.util.*
import kotlinx.parcelize.Parcelize

sealed interface ManageObservationResult
object ManageObservationCanceled : ManageObservationResult
object ManageObservationFinished : ManageObservationResult
data class ManageObservationCreated(val occurenceId: UUID) : ManageObservationResult

@Parcelize
data class CreateObservationResult(val occurenceId: UUID) : Parcelable

object ManageObservation : ActivityResultContract<ObservationAction, ManageObservationResult>() {
    override fun createIntent(context: Context, input: ObservationAction) =
        Intent(context, ObservationActivity::class.java)
            .putExtra(OBSERVATION_ACTION, input)

    override fun parseResult(resultCode: Int, intent: Intent?): ManageObservationResult =
        if (resultCode == Activity.RESULT_OK) {
            intent?.let {
                IntentCompat.getParcelableExtra<CreateObservationResult>(
                    it, CREATE_OBSERVATION_RESULT,
                    CreateObservationResult::class.java
                )?.let { result ->
                    ManageObservationCreated(result.occurenceId)
                }
            } ?: ManageObservationFinished
        } else ManageObservationCanceled

    const val OBSERVATION_ACTION = "observation_action"
    const val CREATE_OBSERVATION_RESULT = "create_observation_result"
}
