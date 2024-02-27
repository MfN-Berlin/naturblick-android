package berlin.mfn.naturblick.ui.info.account

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel

class ResetPasswordViewModel : ViewModel(), Observable {

    private var _password: String? = null

    @get:Bindable
    var password: String? get() = _password
        set(value) {
            if (_password != value) {
                _password = value
                notifyPropertyChanged(BR.password)
                updateValidationStatus()
            }
        }

    fun updateValidationStatus() {
        val password = this.password
        val (passwordIsValid, passwordMessage) = password?.let {
            validatePassword(it)
        } ?: Pair(null, null)
        _validationStatus?.let { cb ->
            cb(passwordIsValid, passwordMessage)
        }
        _isValid?.let { cb ->
            cb(passwordIsValid != null && passwordIsValid)
        }
    }

    private var _validationStatus: ((Boolean?, Int?) -> Unit)? = null
    fun setValidationStatusCallback(cb: (Boolean?, Int?) -> Unit) {
        _validationStatus = cb
    }

    private var _isValid: ((Boolean) -> Unit)? = null
    fun setIsValidCallback(cb: (Boolean) -> Unit) {
        _isValid = cb
    }

    /**
     * Please see:https://developer.android.com/topic/libraries/data-binding/architecture#observable-viewmodel
     */
    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()
    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.remove(callback)
    }

    /**
     * Notifies observers that a specific property has changed. The getter for the
     * property that changes should be marked with the @Bindable annotation to
     * generate a field in the BR class to be used as the fieldId parameter.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }
}
