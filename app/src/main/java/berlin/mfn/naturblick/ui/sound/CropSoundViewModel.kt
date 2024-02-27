package berlin.mfn.naturblick.ui.sound

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.*
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.utils.*
import kotlinx.coroutines.launch

data class SoundView(
    val uri: Uri,
    val remoteMedia: RemoteMedia,
    val spectrogram: Bitmap,
    val isNew: Boolean,
    val initialStart: Int?,
    val initialEnd: Int?
)

class CropSoundViewModel(
    private val app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val _isNew: MutableLiveData<Boolean> = savedStateHandle.getLiveData("is-new")
    private val _media: MutableLiveData<Media> = savedStateHandle.getLiveData("media")
    private val _initialStart: MutableLiveData<Int?> = savedStateHandle.getLiveData("initial-start")
    private val _initialEnd: MutableLiveData<Int?> = savedStateHandle.getLiveData("initial-end")

    fun setMedia(media: Media) {
        _media.value = media
        _isNew.value = true
    }

    fun setRequest(request: CropAndIdentifySoundRequest, isNew: Boolean) {
        _media.value = request.media
        _isNew.value = isNew
        _initialStart.value = request.segmStart
        _initialEnd.value = request.segmEnd
    }

    val media: LiveData<Media> = _media
    private val isNew: LiveData<Boolean> = _isNew

    fun errorOptions(): Int =
        if (isNew.value!!) {
            R.array.autoid_error_options
        } else {
            R.array.autoid_not_new_error_options
        }

    private val _data = MutableLiveData<NetworkResult<SoundView>>()
    val data: LiveData<NetworkResult<SoundView>> = _data

    fun getSpectrogram(uri: Uri) {
        val media = media.value!!
        val isNew = isNew.value!!
        val initialStart = _initialStart.value
        val initialEnd = _initialEnd.value
        viewModelScope.launch {
            _data.postValue(
                media.upload(app).flatMap { remote ->
                    Spectrogram.remote(app, remote).map {
                        SoundView(uri, remote, it, isNew, initialStart, initialEnd)
                    }
                }
            )
        }
    }

    fun getSpectrogram(readPermissionGranted: Boolean) {
        val media = media.value!!
        val isNew = isNew.value!!
        val initialStart = _initialStart.value
        val initialEnd = _initialEnd.value
        viewModelScope.launch {
            _data.postValue(
                media.upload(app).flatMap { remote ->
                    remote.fetchUri(readPermissionGranted, app).flatMap { uri ->
                        Spectrogram.remote(app, remote).map {
                            SoundView(uri, remote, it, isNew, initialStart, initialEnd)
                        }
                    }
                }
            )
        }
    }

    private var _showOnLeaveDialog: (((Boolean) -> Unit) -> Unit)? = null
    fun setShowOnLeaveDialog(showOnLeaveDialog: ((Boolean) -> Unit) -> Unit) {
        _showOnLeaveDialog = showOnLeaveDialog
    }

    fun onLeave(leave: (Boolean) -> Unit) {
        val isNew = isNew.value
        if (isNew == null || isNew) {
            val showDialog = _showOnLeaveDialog
            if (showDialog != null) {
                showDialog(leave)
            } else {
                leave(true)
            }
        } else {
            leave(true)
        }
    }
}

class CropSoundViewModelFactory(
    private val application: Application
) : AbstractSavedStateViewModelFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return CropSoundViewModel(
            application,
            handle
        ) as T
    }
}
