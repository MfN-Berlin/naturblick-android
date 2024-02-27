package berlin.mfn.naturblick.ui.info.account

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.*
import berlin.mfn.naturblick.ui.info.settings.Settings

class AccountViewModel(application: Application) :
    AndroidViewModel(application),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val sp = Settings.sharedPreferences(application)

    private val _neverSignedIn = MutableLiveData<Boolean>()
    val neverSignedIn: LiveData<Boolean> = _neverSignedIn

    private val _email = MutableLiveData<String?>()
    val email: LiveData<String?> = _email

    private var _hasToken = MutableLiveData<Boolean>()
    val hasToken: LiveData<Boolean> = _hasToken

    val fullySignedOut = email.switchMap { email ->
        hasToken.map {
            email == null && !it
        }
    }

    private var _closeOnFinished: Boolean = true
    val closeOnFinished: Boolean get() = _closeOnFinished

    fun setCloseOnFinished(close: Boolean) {
        _closeOnFinished = close
    }

    override fun onSharedPreferenceChanged(p1: SharedPreferences?, p2: String?) {
        _email.postValue(Settings.getEmail(sp))
        _hasToken.postValue(Settings.getToken(sp) != null)
        _neverSignedIn.postValue(Settings.didNeverSignIn(sp))
    }

    init {
        sp.registerOnSharedPreferenceChangeListener(this)
        onSharedPreferenceChanged(sp, "")
    }
    override fun onCleared() {
        super.onCleared()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }
}
