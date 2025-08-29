/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.observation

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.IntentCompat
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.Coordinates
import berlin.mfn.naturblick.ui.BaseActivity
import berlin.mfn.naturblick.ui.fieldbook.CreateObservationResult
import berlin.mfn.naturblick.ui.fieldbook.ManageObservation.CREATE_OBSERVATION_RESULT
import berlin.mfn.naturblick.ui.fieldbook.ManageObservation.OBSERVATION_ACTION
import berlin.mfn.naturblick.ui.fieldbook.ObservationAction
import berlin.mfn.naturblick.ui.fieldbook.PickLocation
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ObservationActivity : BaseActivity(
    R.navigation.observation_navigation
) {
    private lateinit var model: ObservationViewModel

    val pickLocation =
        registerForActivityResult(PickLocation) { coordinates: Coordinates? ->
            coordinates?.let {
                model.coordinatesChanged(coordinates.lat, coordinates.lon)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
        val viewModel: ObservationViewModel by viewModels {
            ObservationViewModel.Factory
        }
        model = viewModel
    }

    override fun onLeave(leave: (Boolean) -> Unit): Boolean =
        if (requiresSave()) {
            saveDialog(
                model.currentObservation.value.isNew(),
                model.currentObservation.value.hasCoordinates()
            ) { isNew ->
                if (isNew) {
                    setResult(
                        RESULT_OK,
                        Intent().apply {
                            putExtra(
                                CREATE_OBSERVATION_RESULT,
                                CreateObservationResult(model.occurenceId)
                            )
                        }
                    )
                } else {
                    setResult(RESULT_OK)
                }
                finish()
            }
            false
        } else {
            leave(true)
            false
        }

    private fun requiresSave(): Boolean =
        model.currentObservation.value.hasChanges()

    fun coordinatesDialog(fetchingLocation: Boolean, done: () -> Unit) {
        MaterialAlertDialogBuilder(this, R.style.Naturblick_MaterialComponents_Dialog_Alert).apply {
            setTitle(R.string.no_location)
            setMessage(
                if (fetchingLocation)
                    R.string.no_location_message
                else
                    R.string.no_location_message_no_wait
            )
            setPositiveButton(R.string.pick_location) { _, _ ->
                pickLocation.launch(null)
            }
            if (fetchingLocation) {
                setNeutralButton(R.string.wait_for_location) { _, _ ->
                }
            }
            setNegativeButton(
                R.string.save_without_location
            ) { _, _ ->
                done()
            }
        }.show()
    }

    private fun saveDialog(isNew: Boolean, hasCoordinates: Boolean, done: (Boolean) -> Unit) {
        val dialogBuild =
            MaterialAlertDialogBuilder(this, R.style.Naturblick_MaterialComponents_Dialog_Alert)

        dialogBuild
            .setTitle(
                if (isNew)
                    R.string.save_observation
                else
                    R.string.save_changes
            )
            .setMessage(
                if (isNew)
                    R.string.save_observation_message
                else
                    R.string.save_changes_message
            )
            .setPositiveButton(R.string.save) { _, _ ->
                if (hasCoordinates || !isNew) {
                    model.save {
                        done(it)
                    }
                } else {
                    coordinatesDialog(model.fetchingLocation.value ?: false) {
                        model.save {
                            done(it)
                        }
                    }
                }
            }
            .setNegativeButton(
                if (isNew)
                    R.string.exit_without_saving_observation
                else
                    R.string.exit_without_saving
            ) { _, _ ->
                done(isNew)
            }
        dialogBuild.show()
    }
}
