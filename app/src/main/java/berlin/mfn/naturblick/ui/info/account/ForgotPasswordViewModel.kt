package berlin.mfn.naturblick.ui.info.account

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel

class ForgotPasswordViewModel : ViewModel(), Observable {

    private var _email: String? = null

    @get:Bindable
    var email: String? get() = _email
        set(value) {
            if (_email != value) {
                _email = value
                notifyPropertyChanged(BR.email)
                updateValidationStatus()
            }
        }

    fun updateValidationStatus() {
        val email = this.email
        val emailIsValid = email?.let {
            validateEmail(it)
        }
        _validationStatus?.let { cb ->
            cb(emailIsValid)
        }
        _isValid?.let { cb ->
            cb(
                emailIsValid != null && emailIsValid
            )
        }
    }

    private var _validationStatus: ((Boolean?) -> Unit)? = null
    fun setValidationStatusCallback(cb: (Boolean?) -> Unit) {
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
