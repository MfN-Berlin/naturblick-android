/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.photo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.IntentCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentConfirmPhotoBinding
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContract
import berlin.mfn.naturblick.ui.idresult.IdentifySpecies
import berlin.mfn.naturblick.ui.idresult.IdentifySpeciesImage
import berlin.mfn.naturblick.ui.info.account.AccountActivity
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.ui.photo.CropAndIdentifyPhoto.CROP_AND_IDENTIFY_RESULT
import berlin.mfn.naturblick.ui.species.PickSpecies
import berlin.mfn.naturblick.utils.*
import berlin.mfn.naturblick.utils.Media.Companion.JPEG_QUALITY
import com.canhub.cropper.CropImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

class TakePicture : ActivityResultContracts.TakePicture() {
    override fun createIntent(context: Context, input: Uri): Intent {
        val intent = super.createIntent(context, input)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION
                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        return intent
    }
}

class ConfirmPhotoFragment :
    Fragment(),
    CropImageView.OnCropImageCompleteListener,
    RequiredPermissionCallback {

    private lateinit var model: ImageIdViewModel
    private lateinit var binding: FragmentConfirmPhotoBinding

    private val requestRead = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val readGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            granted.getOrDefault(
                Manifest.permission.READ_MEDIA_IMAGES,
                false
            )
        } else {
            granted.getOrDefault(
                Manifest.permission.READ_EXTERNAL_STORAGE, false
            )
        }
        if (model.launchGallery()) {
            if (readGranted) {
                launchPicKVisualMedia.launch(arrayOf(MediaType.JPG.mime))
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.read_media_required_to_import,
                    Toast.LENGTH_LONG
                ).show()
                requireActivity().setResult(Activity.RESULT_CANCELED)
                requireActivity().finish()
            }
        } else {
            lifecycleScope.launch {
                model.full?.let { media ->
                    when (media) {
                        is RemoteMedia -> {
                            val result = media.fetchUri(
                                readGranted,
                                requireContext()
                            )
                            val errorDialog = ErrorDialog.forIdFlow(true) { chosen, isSignedOut ->
                                when (chosen) {
                                    0 -> if (isSignedOut)
                                        startActivity(
                                            Intent(requireContext(), AccountActivity::class.java)
                                        )
                                    else
                                        getOriginalImage(media)
                                    1 -> findSpeciesResult.launch(Unit)
                                }
                            }
                            resolveWithErrorDialog(result, errorDialog) {
                                binding.cropImageView.setImageUriAsync(it)
                            }
                        }
                        is LocalMedia ->
                            binding.cropImageView.setImageUriAsync(media.uri)
                    }
                }
            }
        }
    }

    private fun requestReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestRead.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                )
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestRead.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_MEDIA_LOCATION
                )
            )
        } else {
            requestRead.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private val launchPicKVisualMedia =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                lifecycleScope.launch {
                    model.gallerySuccessful(uri)
                    binding.cropImageView.setImageUriAsync(uri)
                }
            } else {
                requireActivity().setResult(Activity.RESULT_CANCELED)
                requireActivity().finish()
            }
        }

    private val launchTakePicture =
        registerForActivityResult(TakePicture()) { isSuccess ->
            if (isSuccess) {
                val uri = model.photoSuccessful()
                requireContext().revokeUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                binding.cropImageView.setImageUriAsync(uri)
            } else {
                model.photoFailed()
                requireActivity().setResult(Activity.RESULT_CANCELED)
                requireActivity().finish()
            }
        }

    private fun finishWithResult(result: CropAndIdentifyPhotoResult) {
        val intent = Intent()
        intent.putExtra(
            CROP_AND_IDENTIFY_RESULT,
            result
        )
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    private val idResultLauncher: ActivityResultLauncher<IdentifySpecies> =
        registerForActivityResult(
            IdResultActivityContract
        ) { result ->
            if (result != null) {
                finishWithResult(
                    model.createCropAndIdentifyPhotoResult(
                        result.speciesId
                    )
                )
            }
        }

    private val findSpeciesResult = registerForActivityResult(PickSpecies) { speciesResult ->
        if (speciesResult != null) {
            finishWithResult(model.createCropAndIdentifyPhotoResult(speciesResult.speciesId))
        }
    }

    private val permissionChecker = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        IdentityRequiredPermissionChecker
    } else {
        registerRequiredPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            R.string.storage_permission_missing,
            this
        )
    }

    private fun getOriginalImage(media: Media) {
        media.availableWithoutPermission(requireContext(), {
            binding.cropImageView.setImageUriAsync(it)
        }, {
            requestReadPermission()
        })
    }

    private fun cropAsync() {
        val boxSize = resources.getDimension(R.dimen.crop_size).roundToInt()
        binding.cropImageView.croppedImageAsync(
            Bitmap.CompressFormat.JPEG,
            JPEG_QUALITY,
            boxSize,
            boxSize,
            CropImageView.RequestSizeOptions.RESIZE_EXACT,
            model.newCrop()
        )
    }

    override fun requiredPermissionGranted() {
        val empty = model.launchCamera()
        if (model.launchGallery()) {
            requestReadPermission()
        } else if (empty != null) {
            launchTakePicture.launch(empty.uri)
        } else {
            model.full?.let {
                getOriginalImage(it)
            }
        }
        binding.buttonConfirm.setSingleClickListener {
            model.setAfterCropAction(::saveAndProceed)
            cropAsync()
        }
        binding.cropImageView.setOnCropImageCompleteListener(this)
        binding.buttonDelete.setSingleClickListener {
            model.onLeave {
                cancel()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val request =
            IntentCompat.getParcelableExtra(
                requireActivity().intent,
                CropAndIdentifyPhoto.CROP_AND_IDENTIFY_REQUEST,
                CropAndIdentifyPhotoRequest::class.java
            )!!
        val imageIdModel by activityViewModels<ImageIdViewModel> {
            ImageIdViewModelFactory(
                request,
                requireActivity().application
            )
        }

        model = imageIdModel

        binding = FragmentConfirmPhotoBinding.inflate(inflater, container, false)
        val cropSize = resources.getDimension(R.dimen.crop_size).roundToInt()
        binding.cropImageView.setMinCropResultSize(cropSize, cropSize)
        permissionChecker.requirePermission(requireContext(), this)
        model.setShowOnLeaveDialog(::showOnLeaveDialog)
        return binding.root
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        Settings.checkCcBy(requireActivity(), layoutInflater) {
            model.cropSuccessful(result.cropRect!!, result.wholeImageRect!!)
        }
    }

    private fun saveAndProceed(
        crop: MediaThumbnail,
        cropRect: Rect,
        full: Media,
        wholeImageRect: Rect
    ) {
        idResultLauncher.launch(
            IdentifySpeciesImage(
                crop,
                !model.isExisting,
                cropRect.left.toFloat() / wholeImageRect.width(),
                cropRect.top.toFloat() / wholeImageRect.height(),
                cropRect.width().toFloat() / wholeImageRect.width(),
                full
            )
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun saveAndFinish(
        crop: MediaThumbnail,
        cropRect: Rect,
        full: Media,
        wholeImageRect: Rect
    ) {
        finishWithResult(
            CropAndIdentifyPhotoResult(
                null,
                full,
                crop
            )
        )
    }

    private fun showOnLeaveDialog(leave: (Boolean) -> Unit) {
        val dialogBuild = MaterialAlertDialogBuilder(requireContext(), R.style.Naturblick_MaterialComponents_Dialog_Alert)

        dialogBuild
            .setTitle(
                R.string.save_observation
            )
            .setMessage(
                R.string.save_observation_message
            )
            .setPositiveButton(R.string.save) { _, _ ->
                model.setAfterCropAction(::saveAndFinish)
                cropAsync()
            }
            .setNegativeButton(
                R.string.exit_without_saving_observation
            ) { _, _ ->
                leave(false)
            }
        dialogBuild.show()
    }
}
