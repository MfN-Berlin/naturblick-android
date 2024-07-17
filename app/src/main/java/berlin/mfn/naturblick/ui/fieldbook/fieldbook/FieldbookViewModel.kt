package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.*
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.utils.MediaThumbnail

class FieldbookViewModel(
    private val savedStateHandle: SavedStateHandle,
    application: Application
) : ViewModel() {
    private val operationDao = ObservationDb.getDb(application).operationDao()
    private val speciesDao = StrapiDb.getDb(application).speciesDao()
    var launched: Boolean
        get() = savedStateHandle["launched"] ?: false
        set(value) {
            savedStateHandle["launched"] = value
        }

    val pagingData =
        Pager(PAGING_CONFIG) {
            operationDao.getObservationsPagingSource()
        }.liveData.cachedIn(this).map {
            it.map {
                if (it.newSpeciesId != null)
                    FieldbookObservation(
                        it.occurenceId,
                        it.created,
                        it.thumbnailId?.let { thumbnailId ->
                            MediaThumbnail.remote(thumbnailId, it.obsIdent)
                        },
                        it.obsIdent,
                        speciesDao.getSpecies(it.newSpeciesId)
                    )
                else
                    FieldbookObservation(
                        it.occurenceId,
                        it.created,
                        it.thumbnailId?.let { thumbnailId ->
                            MediaThumbnail.remote(thumbnailId, it.obsIdent)
                        },
                        it.obsIdent,
                        null
                    )
            }
        }

    companion object {
        val PAGING_CONFIG = PagingConfig(50)
    }
}

class FieldbookViewModelFactory(
    private val application: Application
) : AbstractSavedStateViewModelFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return FieldbookViewModel(
            handle,
            application
        ) as T
    }
}
