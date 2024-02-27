package berlin.mfn.naturblick.ui.sound

import androidx.lifecycle.ViewModel

class RecordSoundViewModel : ViewModel() {
    private var _start: (() -> Unit)? = null
    private var _granted: Boolean = false
    private var _stopped: Boolean = false
    fun setStart(start: () -> Unit) {
        _start = start
    }

    fun stop() {
        _stopped = true
    }

    private fun start() {
        _stopped = false
        _start?.let {
            it()
        }
    }

    fun resumed() {
        if (_granted && _stopped) {
            start()
        }
    }

    fun permissionGranted() {
        if (!_granted) {
            _granted = true
            start()
        }
    }
}
