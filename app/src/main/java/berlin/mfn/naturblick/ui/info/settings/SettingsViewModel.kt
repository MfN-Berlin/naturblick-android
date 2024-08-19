/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.settings

import android.app.Activity
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SettingsViewModel(val activity: Activity) : AndroidViewModel(activity.application) {
    val ccByName = ObservableField(Settings.getCcBy(activity.application))

    fun save() {
        ccByName.get()?.let {
            Settings.setCcBy(activity, it)
        }
    }
}

class SettingsViewModelFactory(
    private val activity: Activity
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(
        activity
    ) as T
}
