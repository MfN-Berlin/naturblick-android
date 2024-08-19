/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.idresult

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import berlin.mfn.naturblick.room.StrapiDb

class SpeciesInfoViewModel(private val speciesId: Int, application: Application) :
    AndroidViewModel(application) {
    val species = liveData {
        emit(StrapiDb.getDb(application).speciesDao().getSpecies(speciesId))
    }
}

class SpeciesInfoViewModelFactory(
    private val speciesId: Int,
    private val application: Application
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpeciesInfoViewModel(speciesId, application) as T
}
