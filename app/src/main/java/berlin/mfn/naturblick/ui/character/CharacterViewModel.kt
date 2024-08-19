/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package berlin.mfn.naturblick.ui.character

import android.app.Application
import androidx.lifecycle.*
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.utils.languageId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.launch

class CharacterViewModel(group: String, application: Application) : AndroidViewModel(application) {
    private val speciesDao = StrapiDb.getDb(application).speciesDao()
    private val characters = flow {
        emit(StrapiDb.getDb(application).characterDao().getCharacters(group))
    }

    private val toggle = MutableSharedFlow<Int>()

    fun toggleValue(id: Int) {
        viewModelScope.launch {
            toggle.emit(id)
        }
    }

    private val selectedState = characters.flatMapConcat { characterWithValues ->
        toggle.runningFold(emptySet()) { state: Set<Int>, toggled: Int ->
            state.minus(
                characterWithValues.flatMap {
                    it.requiresUnset(toggled)
                }.toSet()
            ).run {
                if (contains(toggled)) {
                    minus(toggled)
                } else {
                    plus(toggled)
                }
            }
        }.map {
            CharacterValuesState(characterWithValues, it)
        }
    }.shareIn(viewModelScope, Eagerly)

    val state = selectedState.asLiveData()

    val numberOfResults = selectedState.map {
        val query = it.query
        speciesDao.countSpeciesByCharacters(null, languageId(), query.number, query.query)
    }.asLiveData()
}

class CharacterViewModelFactory(
    private val group: String,
    private val application: Application
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CharacterViewModel(
        group,
        application
    ) as T
}
