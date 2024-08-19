/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.account

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class SignUpViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel(), Observable {

    @get:Bindable
    var email: String? get() = savedStateHandle["email"]
        set(value) {
            val existingEmail: String? = savedStateHandle["email"]
            if (existingEmail != value) {
                savedStateHandle["email"] = value
                notifyPropertyChanged(BR.email)
                updateValidationStatus()
            }
        }
    @get:Bindable
    var password: String? get() = savedStateHandle["password"]
        set(value) {
            val existingPassword: String? = savedStateHandle["password"]
            if (existingPassword != value) {
                savedStateHandle["password"] = value
                notifyPropertyChanged(BR.password)
                updateValidationStatus()
            }
        }

    @get:Bindable
    var privacy: Boolean get() = savedStateHandle["privacy"] ?: false
        set(value) {
            val existingPrivacy: Boolean? = savedStateHandle["privacy"]
            if (existingPrivacy != value) {
                savedStateHandle["privacy"] = value
                notifyPropertyChanged(BR.privacy)
                updateValidationStatus()
            }
        }

    fun updateValidationStatus() {
        val email = this.email
        val emailIsValid = email?.let {
            validateEmail(it)
        }
        val password = this.password
        val (passwordIsValid, passwordMessage) = password?.let {
            validatePassword(it)
        } ?: Pair(null, null)
        _validationStatus?.let { cb ->
            cb(emailIsValid, passwordIsValid, passwordMessage)
        }
        _isValid?.let { cb ->
            cb(
                emailIsValid != null && emailIsValid &&
                    passwordIsValid != null && passwordIsValid &&
                    privacy
            )
        }
    }

    private var _validationStatus: ((Boolean?, Boolean?, Int?) -> Unit)? = null
    fun setValidationStatusCallback(cb: (Boolean?, Boolean?, Int?) -> Unit) {
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
