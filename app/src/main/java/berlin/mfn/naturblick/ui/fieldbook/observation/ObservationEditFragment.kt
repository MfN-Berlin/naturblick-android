/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.observation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.DialogTimeBinding
import berlin.mfn.naturblick.databinding.FragmentObservationEditBinding
import berlin.mfn.naturblick.ui.fieldbook.*
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContract
import berlin.mfn.naturblick.ui.idresult.IdentifySpeciesImageThumbnail
import berlin.mfn.naturblick.ui.photo.*
import berlin.mfn.naturblick.ui.sound.CropAndIdentifySound
import berlin.mfn.naturblick.ui.sound.CropAndIdentifySoundRequest
import berlin.mfn.naturblick.ui.sound.CropAndIdentifySoundResult
import berlin.mfn.naturblick.ui.species.PickSpecies
import berlin.mfn.naturblick.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import kotlinx.coroutines.launch

class ObservationEditFragment : Fragment(), RequestedPermissionsCallback {
    private lateinit var viewModel: ObservationViewModel
    private lateinit var locationProvider: LocationProvider

    private val pickSpecies = registerForActivityResult(PickSpecies) {
        checkResult(it) { speciesResult ->
            viewModel.speciesChanged(speciesResult.speciesId)
            true
        }
    }

    private val imageId = registerForActivityResult(CropAndIdentifyPhoto) {
        checkResult(it, ::updateWithPhotoResult)
    }

    private val soundId = registerForActivityResult(CropAndIdentifySound) {
        checkResult(it, ::updateWithSoundResult)
    }

    private val thumbnailImageId = registerForActivityResult(IdResultActivityContract) {
        it?.speciesId?.let { speciesId ->
            viewModel.speciesChanged(speciesId)
        }
    }

    private fun <T> checkResult(result: T?, f: (result: T) -> Boolean) {
        if (result == null && viewModel.currentObservation.value.isEmpty()) {
            requireActivity().setResult(RESULT_CANCELED)
            requireActivity().finish()
        } else {
            if (result != null) {
                if (f(result))
                    needLocation()
            }
        }
    }

    private fun updateWithSoundResult(result: CropAndIdentifySoundResult): Boolean {
        if (result.speciesId != null) {
            viewModel.speciesChanged(result.speciesId)
        }
        viewModel.mediaChanged(result.media, result.thumbnail)
        return true
    }

    private fun updateWithPhotoResult(result: CropAndIdentifyPhotoResult): Boolean {
        if (result.speciesId != null) {
            viewModel.speciesChanged(result.speciesId)
        }
        viewModel.mediaChanged(result.media, result.thumbnail)

        if (result.speciesId != null) {
            viewModel.speciesChanged(result.speciesId)
        }
        val meta = result.media.meta
        if (meta is ImportedMediaMeta) {
            timeDialog()
        }

        return result.media.meta.fetchCoordinates
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: ObservationViewModel by activityViewModels()
        viewModel.setRequestReadPermission {
            requestReadPermission.checkPermission(requireContext(), this)
        }
        this.viewModel = viewModel
        val binding = FragmentObservationEditBinding.inflate(inflater, container, false)
        binding.model = viewModel
        binding.include.model = viewModel

        binding.bottomSheet.setUpRootAndTopSheet(
            binding.root,
            binding.include.root
        )
        binding.buttonSheet.setupBottomInset()
        binding.fetchingLocation = viewModel.fetchingLocation

        lifecycleScope.launch {
            viewModel.media.collect { (media, _) ->
                binding.buttonSelectSpecies.setSingleClickListener {
                    viewModel.changeSpecies(media)
                }
            }
        }

        binding.save.setSingleClickListener {
            if (viewModel.currentObservation.value.hasCoordinates() ||
                !viewModel.currentObservation.value.isNew()
            ) {
                viewModel.save { isNew ->
                    if (isNew) {
                        requireActivity().setResult(
                            RESULT_OK,
                            Intent().apply {
                                putExtra(
                                    ManageObservation.CREATE_OBSERVATION_RESULT,
                                    CreateObservationResult(viewModel.occurenceId)
                                )
                            }
                        )
                    } else {
                        requireActivity().setResult(RESULT_OK)
                    }
                    requireActivity().finish()
                }
            } else {
                (requireActivity() as ObservationActivity)
                    .coordinatesDialog(viewModel.fetchingLocation.value ?: false) {
                        viewModel.save {
                            requireActivity().setResult(RESULT_OK)
                            requireActivity().finish()
                        }
                    }
            }
        }

        binding.delete.setSingleClickListener {
            deleteDialog {
                requireActivity().finish()
            }
        }

        locationProvider = LocationProvider.getProvider(requireActivity())

        if (!viewModel.createFlowLaunched) { // Only execute actions on initial start
            viewModel.createFlowLaunched = true
            when (val action = viewModel.action) {
                is CreateImageObservation -> {
                    imageId.launch(CropAndIdentifyNewPhotoRequest)
                }
                is CreateImageFromGalleryObservation -> {
                    imageId.launch(CropAndIdentifyGalleryRequest)
                }
                is CreateManualObservation -> {
                    if (action.speciesId == null) {
                        pickSpecies.launch(Unit)
                    } else {
                        needLocation()
                    }
                }
                is CreateAudioObservation -> {
                    soundId.launch(null)
                }
                else -> {}
            }
        }
        viewModel.setChangeBehavior { behaviors, selected ->
            val index = behaviors.indexOf(selected)
            var behavior: Behavior? = null
            MaterialAlertDialogBuilder(requireContext(), R.style.Naturblick_MaterialComponents_Dialog_Alert)
                .setTitle(R.string.observation)
                .setSingleChoiceItems(behaviors, index) { _: DialogInterface, i: Int ->
                    if (i != -1) {
                        behavior = Behavior.parse(requireContext(), behaviors[i])
                    }
                }
                .setPositiveButton(R.string.save) { _, _ ->
                    behavior?.let {
                        viewModel.behaviorChanged(it.value)
                    }
                }
                .setNeutralButton(R.string.cancel, null)
                .show()
        }
        viewModel.setChangeLocation {
            (requireActivity() as ObservationActivity).pickLocation.launch(it)
        }
        viewModel.setPickSpecies {
            pickSpecies.launch(Unit)
        }
        viewModel.setImageId { media, thumbnail ->
            if (media != null) {
                imageId.launch(CropAndIdentifyMediaRequest(media))
            } else {
                thumbnail?.let {
                    thumbnailImageId.launch(
                        IdentifySpeciesImageThumbnail(it)
                    )
                }
            }
        }
        viewModel.setSoundId { media, segmStart, segmEnd ->
            soundId.launch(CropAndIdentifySoundRequest(media, segmStart, segmEnd))
        }

        binding.fetchingLocation = viewModel.fetchingLocation
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun needLocation() {
        if (
            !viewModel.currentObservation.value.hasCoordinates() &&
            viewModel.action != CreateImageFromGalleryObservation
        ) {
            requestLocationPermission.checkPermission(requireContext(), this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(retries: Int = 3) {
        viewModel.setFetchingLocation(true)
        if (retries > 0) {
            locationProvider.getCurrentLocation(
                requireActivity(), { location ->
                    viewModel.coordinatesChanged(
                        location.latitude,
                        location.longitude
                    )
                    viewModel.setFetchingLocation(false)
                },
                {
                    viewModel.setFetchingLocation(false)
                },
                {
                    getLocation(retries - 1)
                    Log.e(TAG, "Error getting location will retry")
                }
            )
        } else {
            viewModel.setFetchingLocation(false)
            Log.e(TAG, "Gave up getting location")
        }
    }

    private fun timeDialog() {
        val dialogBuild = MaterialAlertDialogBuilder(requireContext(), R.style.Naturblick_MaterialComponents_Dialog_Alert)
        val binding = DialogTimeBinding.inflate(requireActivity().layoutInflater)
        binding.model = viewModel
        binding.timeZoneSpinner.adapter = ArrayAdapter(
            requireContext(),
            R.layout.view_time_zone,
            R.id.time_zone_text_view,
            TimeZone.getAvailableIDs().map {
                ZoneId.of(it).getDisplayName(TextStyle.FULL, Locale.getDefault())
            }
        )
        binding.timeZoneSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.timezoneChanged(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                throw IllegalStateException("Time zone deselected")
            }
        }
        binding.editTimeInput.setSingleClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    viewModel.timeChanged(hourOfDay, minute)
                },
                viewModel.currentObservation.value.createdState?.hour!!,
                viewModel.currentObservation.value.createdState?.minute!!,
                is24HourFormat(activity)
            ).show()
        }
        binding.editDateInput.setSingleClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    viewModel.dateChanged(year, month + 1, dayOfMonth)
                },
                viewModel.currentObservation.value.createdState?.year!!,
                viewModel.currentObservation.value.createdState?.monthValue!! - 1,
                viewModel.currentObservation.value.createdState?.dayOfMonth!!
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
        dialogBuild
            .setCancelable(false)
            .setTitle(R.string.validate_time)
            .setView(binding.root)
            .setPositiveButton(R.string.set_time) { _, _ ->
            }
        binding.lifecycleOwner = viewLifecycleOwner
        dialogBuild.show()
    }

    private fun deleteDialog(done: () -> Unit) {
        val dialogBuild = MaterialAlertDialogBuilder(requireContext(), R.style.Naturblick_MaterialComponents_Dialog_Alert)

        dialogBuild
            .setTitle(R.string.delete_question)
            .setMessage(R.string.delete_question_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteWithCallback {
                    done()
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                // On cancel nothing is done
            }
        dialogBuild.show()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    companion object {
        const val TAG = "ObservationFragment"
    }

    override fun permissionResult(granted: Boolean, permissions: List<String>) {
        if (granted &&
            (
                permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION)
                )
        ) {
            getLocation()
        } else if (permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            viewModel.readPermissionResult(granted)
        }
    }

    private val requestLocationPermission =
        registerRequestedOneOfPermission(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            this
        )

    private val requestReadPermission = registerLocalMediaReadPermissionRequest(
        this
    )
}
