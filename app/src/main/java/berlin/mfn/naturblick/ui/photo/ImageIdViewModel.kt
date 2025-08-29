/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.photo

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import berlin.mfn.naturblick.utils.*

class ImageIdViewModel(
    private val request: CropAndIdentifyPhotoRequest,
    private val savedStateHandle: SavedStateHandle,
    private val application: Application
) : ViewModel() {

    private var emptyCrop: EmptyLocalMediaThumbnail?
        get() = savedStateHandle["emptyLocalMediaThumbnail"]
        set(value) {
            savedStateHandle["emptyLocalMediaThumbnail"] = value
        }
    private var crop: LocalMediaThumbnail?
        get() = savedStateHandle["localMediaThumbnail"]
        set(value) {
            savedStateHandle["localMediaThumbnail"] = value
        }
    private var emptyFull: EmptyLocalMedia?
        get() = savedStateHandle["emptyFull"]
        set(value) {
            savedStateHandle["emptyFull"] = value
        }
    private val existing = when (request) {
        is CropAndIdentifyMediaRequest ->
            request.media
        else -> null
    }

    private var _full: Media?
        get() = existing ?: savedStateHandle["full"]
        set(value) {
            savedStateHandle["full"] = value
        }

    val isExisting = existing != null

    fun newCrop(): Uri {
        val newCrop = MediaThumbnail.createEmpty(application)
        emptyCrop = newCrop
        return newCrop.file.toUri()
    }

    private var afterCropAction:
        ((MediaThumbnail, Media) -> Unit)? = null

    fun setAfterCropAction(
        afterCropAction: (MediaThumbnail, Media) -> Unit
    ) {
        this.afterCropAction = afterCropAction
    }

    fun cropSuccessful() {
        crop = emptyCrop?.externallySuccessfullyCreated()
        emptyCrop = null
        afterCropAction?.let {
            it(crop!!, _full!!)
        }
    }

    suspend fun gallerySuccessful(uri: Uri) {
        _full = Media.createFromGallery(application, uri)
    }

    fun launchGallery(): Boolean = request is CropAndIdentifyGalleryRequest && _full == null

    fun launchCamera(): EmptyLocalMedia? =
        if (request is CropAndIdentifyNewPhotoRequest && emptyFull == null && _full == null) {
            emptyFull = Media.createEmpty(MediaType.JPG, application)
            emptyFull
        } else {
            null
        }

    fun photoSuccessful(): Uri {
        val completed = emptyFull!!.externallySuccessfullyCreated(application)
        _full = completed
        emptyFull = null
        return completed.uri
    }

    fun photoFailed() {
        emptyFull?.externallyFailed(application)
        emptyFull = null
    }

    val full: Media? get() = _full

    fun createCropAndIdentifyPhotoResult(speciesId: Int?) =
        CropAndIdentifyPhotoResult(
            speciesId,
            full!!,
            crop
        )

    private var _showOnLeaveDialog: (((Boolean) -> Unit) -> Unit)? = null

    fun setShowOnLeaveDialog(showOnLeaveDialog: ((Boolean) -> Unit) -> Unit) {
        _showOnLeaveDialog = showOnLeaveDialog
    }

    fun onLeave(leave: (Boolean) -> Unit): Boolean =
        if (isExisting || _full == null) {
            emptyFull?.externallyFailed(application)
            leave(true)
            false
        } else {
            _showOnLeaveDialog?.let {
                it(leave)
            }
            false
        }

    companion object {
        const val TAG = "ImageIdViewModel"
        val Factory = viewModelFactory {
                initializer {
                    val savedStateHandle = createSavedStateHandle()
                    val action: CropAndIdentifyPhotoRequest = savedStateHandle[CropAndIdentifyPhoto.CROP_AND_IDENTIFY_REQUEST]!!
                    ImageIdViewModel(action, savedStateHandle, (this[APPLICATION_KEY] as Application))
                }
            }
    }
}

