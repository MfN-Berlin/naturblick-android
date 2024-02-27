package berlin.mfn.naturblick.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TrackingViewModel : ViewModel() {
    private val _track = MutableLiveData<Boolean>(false)
    val track: LiveData<Boolean> = _track

    fun startTracking() {
        val track = track.value
        if (track == null || !track) {
            _track.value = true
            trackingStartedCallback?.let {
                it()
            }
        }
    }

    fun stopTracking() {
        val track = track.value
        if (track != null && track) {
            _track.value = false
            trackingStoppedCallback?.let {
                it()
            }
        }
    }

    private var trackingStoppedCallback: (() -> Unit)? = null

    fun setTrackingStoppedCallback(f: () -> Unit) {
        trackingStoppedCallback = f
    }

    private var trackingStartedCallback: (() -> Unit)? = null

    fun setTrackingStartedCallback(f: () -> Unit) {
        trackingStartedCallback = f
    }
}
