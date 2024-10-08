/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.specieslist

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.liveData
import berlin.mfn.naturblick.room.SpeciesDao
import berlin.mfn.naturblick.ui.species.CharacterQuery
import berlin.mfn.naturblick.utils.languageId

class SpeciesListViewModel(private val speciesDao: SpeciesDao) : ViewModel() {
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

    private val _charcters: MutableLiveData<CharacterQuery?> = MutableLiveData()
    fun setCharacters(characters: CharacterQuery?) {
        _charcters.value = characters
    }

    val pagingData = _group.switchMap { group ->
        _charcters.switchMap { characters ->
            _query.switchMap { query ->
                Pager(PAGING_CONFIG) {
                    val searchQuery = if (query != null) "%$query%" else null
                    if (group != null) {
                        speciesDao.filterSpeciesWithPortrait(group, searchQuery, languageId())
                    } else {
                        if (characters != null) {
                            speciesDao.filterSpeciesByCharacters(
                                searchQuery,
                                languageId(),
                                characters.number,
                                characters.query
                            )
                        } else {
                            speciesDao.filterSpecies(searchQuery, languageId())
                        }
                    }
                }.liveData.cachedIn(this)
            }
        }
    }

    companion object {
        val PAGING_CONFIG = PagingConfig(50)
    }
}

class SpeciesListViewModelFactory(private val speciesDao: SpeciesDao) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpeciesListViewModel(speciesDao) as T
}
