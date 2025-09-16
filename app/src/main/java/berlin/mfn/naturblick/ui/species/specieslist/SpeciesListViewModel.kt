/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.specieslist

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import berlin.mfn.naturblick.room.SpeciesDao
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.species.CharacterQuery
import berlin.mfn.naturblick.utils.languageId
import berlin.mfn.naturblick.utils.toSQLLikeQuery

class SpeciesListViewModel(application: Application) : ViewModel() {
    private val speciesDao = StrapiDb.getDb(application).speciesDao()
    private val _query: MutableLiveData<String?> = MutableLiveData<String?>(null)

    fun setQuery(query: String?) {
        if (query != null) {
            _query.value = query
        }
    }

    fun resetQuery() {
        _query.value = null
    }

    private val _group: MutableLiveData<String?> = MutableLiveData()
    fun setGroup(group: String?) {
        _group.value = group
    }

    private val _nature: MutableLiveData<String?> = MutableLiveData()
    fun setNature(nature: String?) {
        _nature.value = nature
    }

    private val _charcters: MutableLiveData<CharacterQuery?> = MutableLiveData()
    fun setCharacters(characters: CharacterQuery?) {
        _charcters.value = characters
    }

    val pagingData = _group.switchMap { group ->
        _charcters.switchMap { characters ->
            _query.switchMap { query ->
                _nature.switchMap { nature ->
                    Pager(PAGING_CONFIG) {
                        val searchQuery = query?.toSQLLikeQuery()
                        if (group != null) {
                            speciesDao.filterSpeciesWithPortrait(group, searchQuery, languageId())
                        } else if (nature != null) {
                            speciesDao.filterSpeciesByNature(nature, searchQuery, languageId())
                        } else if (characters != null) {
                            speciesDao.filterSpeciesByCharacters(
                                searchQuery,
                                languageId(),
                                characters.number,
                                characters.query
                            )
                        } else {
                            speciesDao.filterSpecies(searchQuery, languageId())
                        }
                    }.liveData.cachedIn(this)
                }
            }
        }
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(50)
        val Factory = viewModelFactory {
                initializer {
                    SpeciesListViewModel((this[APPLICATION_KEY] as Application))
                }
            }
    }
}
