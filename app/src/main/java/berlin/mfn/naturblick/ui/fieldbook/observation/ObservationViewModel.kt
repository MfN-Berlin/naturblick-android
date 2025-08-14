/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package berlin.mfn.naturblick.ui.fieldbook.observation

import android.app.Application
import androidx.lifecycle.*
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.*
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.fieldbook.*
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MediaData(
    val media: Media?,
    val granted: Boolean?,
    val startAudio: Boolean,
    val start: Int?,
    val end: Int?
)

class ObservationViewModel(
    val action: ObservationAction,
    private val savedStateHandle: SavedStateHandle,
    private val app: Application
) : AndroidViewModel(app) {
    private val operationDao = ObservationDb.getDb(app).operationDao()
    private val speciesDao = StrapiDb.getDb(app).speciesDao()

    private val _fetchingLocation = MutableLiveData(false)
    val fetchingLocation: LiveData<Boolean> = _fetchingLocation
    fun setFetchingLocation(fetchingLocation: Boolean) {
        _fetchingLocation.value = fetchingLocation
    }

    val occurenceId = when (action) {
        is OpenObservation -> action.occurenceId
        else -> UUID.randomUUID()
    }

    private val initial = operationDao.getObservationFlow(occurenceId).map {
        if (it != null) {
            ObservationUpdate(it)
        } else {
            when (action) {
                is OpenObservation ->
                    // Observation was deleted
                    ObservationUpdate()
                is CreateManualObservation -> ObservationUpdate(
                    null, created = ZonedDateTime.now(),
                    speciesId = action.speciesId,
                    obsType = ObsType.MANUAL
                )
                is CreateImageObservation -> ObservationUpdate(
                    null, obsType = ObsType.UNIDENTIFIED_IMAGE
                )
                is CreateImageFromGalleryObservation -> ObservationUpdate(
                    null, obsType = ObsType.UNIDENTIFIED_IMAGE
                )
                is CreateAudioObservation -> ObservationUpdate(
                    null, obsType = ObsType.UNIDENTIFIED_AUDIO
                )
            }
        }
    }
    val isCreateFlow = action !is OpenObservation

    var createFlowLaunched: Boolean
        get() = savedStateHandle["launched"] ?: false
        set(_) {
            savedStateHandle["launched"] = true
        }

    private var _changeLocation: ((Coordinates?) -> Unit)? = null

    fun setChangeLocation(f: (Coordinates?) -> Unit) {
        _changeLocation = f
    }

    fun changeLocation(c: Coordinates?) {
        _changeLocation?.let {
            it(c)
        }
    }


    private fun getBehaviorList(): Array<String> =
        currentObservationAndSpecies.value?.let { observation ->
            observation.species?.group?.let { group ->
                val isPlant = group == "herb" || group == "tree" || group == "conifer"
                if (isPlant) {
                    app.resources.getStringArray(R.array.plant_behavior)
                } else {
                    app.resources.getStringArray(R.array.animal_behavior)
                }
            }
        } ?: app.resources.getStringArray(R.array.animal_or_plant_behavior)

    private var _changeBehavior: ((Array<String>, String?) -> Unit)? = null

    fun setChangeBehavior(changeBehavior: ((Array<String>, String?) -> Unit)) {
        _changeBehavior = changeBehavior
    }

    fun changeBehavior(selected: String?) {
        _changeBehavior?.let {
            it(getBehaviorList(), selected)
        }
    }

    fun changeSpecies(media: Media?) {
        viewModelScope.launch {
            val observation = currentObservation.value
            when (observation.obsTypeState) {
                ObsType.MANUAL -> _pickSpecies?.let { it() }
                ObsType.AUDIO -> if (media != null) {
                    _soundId?.let {
                        it(
                            media,
                            currentObservation.value.observation?.segmStart,
                            currentObservation.value.observation?.segmEnd
                        )
                    }
                }
                ObsType.UNIDENTIFIED_AUDIO -> if (media != null) {
                    _soundId?.let {
                        it(
                            media,
                            currentObservation.value.observation?.segmStart,
                            currentObservation.value.observation?.segmEnd
                        )
                    }
                }
                ObsType.IMAGE -> _imageId?.let { it(media, observation.thumbnailState) }
                ObsType.UNIDENTIFIED_IMAGE -> _imageId?.let {
                    it(media, observation.thumbnailState)
                }
            }
        }
    }

    private var _pickSpecies: (() -> Unit)? = null
    fun setPickSpecies(f: () -> Unit) {
        _pickSpecies = f
    }

    private var _imageId: ((media: Media?, thumbnail: MediaThumbnail?) -> Unit)? = null
    fun setImageId(f: (media: Media?, thumbnail: MediaThumbnail?) -> Unit) {
        _imageId = f
    }

    private var _soundId: ((media: Media, segmStart: Int?, segmEnd: Int?) -> Unit)? = null
    fun setSoundId(f: (media: Media, segmStart: Int?, segmEnd: Int?) -> Unit) {
        _soundId = f
    }

    fun speciesChanged(speciesId: Int) {
        viewModelScope.launch {
            when (currentObservation.value.obsTypeState) {
                ObsType.UNIDENTIFIED_IMAGE -> {
                    editUpdates.emit(
                        ObservationUpdate(
                            speciesId = speciesId,
                            obsType = ObsType.IMAGE
                        )
                    )
                }
                ObsType.UNIDENTIFIED_AUDIO -> {
                    editUpdates.emit(
                        ObservationUpdate(speciesId = speciesId, obsType = ObsType.AUDIO)
                    )
                }
                else -> {
                    editUpdates.emit(ObservationUpdate(speciesId = speciesId))
                }
            }
        }
    }

    fun mediaChanged(media: Media?, thumbnail: MediaThumbnail?) {
        viewModelScope.launch {
            val created = when (val meta = media?.meta) {
                is CreatedMediaMeta -> meta.created
                is ImportedMediaMeta -> {
                    val zoneId = meta.coordinates?.let { coordinates ->
                        coordinates.getTimeZone(getApplication())
                    }
                    val createdWithTz = if (zoneId != null)
                        meta.created?.atZone(zoneId)
                    else
                        meta.created?.atZone(ZoneId.systemDefault())
                    createdWithTz ?: ZonedDateTime.now()
                }
                else -> null
            }
            val newCoordinates = when (val meta = media?.meta) {
                is CreatedMediaMeta -> meta.coordinates
                is ImportedMediaMeta -> meta.coordinates
                else -> null
            }
            editUpdates.emit(
                ObservationUpdate(
                    created = created,
                    media = media,
                    thumbnail = thumbnail,
                    coordinates = newCoordinates
                )
            )
        }
    }

    fun detailsChanged(s: String) {
        viewModelScope.launch {
            editUpdates.emit(ObservationUpdate(details = s.ifEmpty { "" }))
        }
    }

    fun behaviorChanged(s: String) {
        viewModelScope.launch {
            editUpdates.emit(ObservationUpdate(behavior = s.ifEmpty { "" }))
        }
    }

    fun individualsChanged(s: String) {
        val individuals = try {
            s.toInt()
        } catch (n: NumberFormatException) {
            1
        }
        viewModelScope.launch {
            editUpdates.emit(
                ObservationUpdate(individuals = if (individuals > 1) individuals else 1)
            )
        }
    }

    fun coordinatesChanged(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            editUpdates.emit(ObservationUpdate(coordinates = Coordinates(latitude, longitude)))
        }
    }

    fun timeChanged(hourOfDay: Int, minute: Int) {
        val newDateTime =
            currentObservation.value.createdState?.withHour(hourOfDay)?.withMinute(minute)
        viewModelScope.launch {
            editUpdates.emit(ObservationUpdate(created = newDateTime))
        }
    }

    fun dateChanged(year: Int, month: Int, dayOfMonth: Int) {
        val newDateTime = currentObservation.value.createdState?.withYear(year)?.withMonth(month)
            ?.withDayOfMonth(dayOfMonth)
        viewModelScope.launch {
            editUpdates.emit(ObservationUpdate(created = newDateTime))
        }
    }

    fun timezoneChanged(timeZone: Int) {
        val zoneId = ZoneId.of(TimeZone.getAvailableIDs()[timeZone])
        if (currentObservation.value.createdState?.zone != zoneId) {
            val newDateTime = currentObservation.value.createdState?.withZoneSameLocal(zoneId)
            viewModelScope.launch {
                editUpdates.emit(ObservationUpdate(created = newDateTime))
            }
        }
    }

    // This function requires read permission
    private suspend fun createLegacyImage(obsIdent: String?, localMediaId: String?): RemoteMedia? =
        Media.createLegacyImage(
            getApplication(), obsIdent, localMediaId
        )?.let { media ->
            operationDao.insertOperation(
                PatchOperation(
                    occurenceId = occurenceId, mediaId = media.id, localMediaId = media.localMediaId
                )
            )
            operationDao.refreshObservations()
            media
        }

    fun save(done: ((Boolean) -> Unit)? = null) {
        val new = currentObservation.value.isNew()
        viewModelScope.launch {
            val application = getApplication<NaturblickApplication>()
            val operations = currentObservation.value.toOperations(
                application,
                occurenceId,
                Settings.getCcBy(application),
                appVersion = BuildConfig.VERSION_NAME,
                deviceIdentifier = AndroidDeviceId.deviceId(application.contentResolver)
            )
            operations.forEach { op -> operationDao.insertOperation(op) }
            operationDao.refreshObservations()
            done?.let { it(new) }
        }
    }

    fun deleteWithCallback(done: (() -> Unit)? = null) {
        viewModelScope.launch {
            if (!currentObservation.value.isNew()) {
                operationDao.insertOperation(DeleteOperation(occurenceId))
                operationDao.refreshObservations()
                done?.let { it() }
            } else {
                done?.let { it() }
            }
        }
    }

    private val editUpdates = MutableSharedFlow<ObservationUpdate>()
    val currentObservation = updateFlow(initial, editUpdates).stateIn(
        viewModelScope, SharingStarted.Lazily, ObservationUpdate.EMPTY
    )

    val currentObservationAndSpecies =
        currentObservation.map { observation ->
            val speciesId = observation.speciesIdState

            val species = speciesId?.let {
                speciesDao.getAcceptedSpecies(it)
            }

            observation.toFieldbookObservation(
                occurenceId,
                species
            )
        }.asLiveData()

    val createdTimeStr = currentObservation.map {
        it.createdState?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    }.asLiveData()

    val createdDateStr = currentObservation.map {
        it.createdState?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    }.asLiveData()

    val createdTimeZonePosition: LiveData<Int> = currentObservation.map {
        it.createdState?.let { created ->
            TimeZone.getAvailableIDs().indexOf(created.zone.id)
        } ?: TimeZone.getAvailableIDs().indexOf("Europe/Berlin")
    }.asLiveData()

    private var _requestReadPermission: (() -> Unit)? = null
    fun setRequestReadPermission(f: () -> Unit) {
        _requestReadPermission = f
    }

    private val _gotReadPermission = MutableSharedFlow<Boolean?>(1)

    init {
        viewModelScope.launch {
            _gotReadPermission.emit(null)
        }
    }

    fun readPermissionResult(granted: Boolean) {
        viewModelScope.launch {
            _gotReadPermission.emit(granted)
        }
    }

    private var _startAudioOnPermissionResult: Boolean = false

    fun startAudioOnPermissionResult() {
        _startAudioOnPermissionResult = true
    }

    fun clearStartAudioOnPermissionResult() {
        _startAudioOnPermissionResult = false
    }

    val media: Flow<MediaData> =
        _gotReadPermission.flatMapLatest { gotReadPermission ->
            currentObservation.filterNot {
                it == ObservationUpdate.EMPTY
            }.distinctUntilChanged { old, new ->
                old.mediaState == new.mediaState &&
                    old.observation?.obsIdent == new.observation?.obsIdent &&
                    old.observation?.localMediaId == new.observation?.localMediaId
            }.map { observation ->
                val remoteMedia = if (gotReadPermission != null && gotReadPermission) {
                    // We have read permission and can search for old media
                    if (observation.mediaState == null && observation.isImage()) {
                        createLegacyImage(
                            observation.observation?.obsIdent,
                            observation.observation?.localMediaId
                        )
                    } else {
                        observation.mediaState
                    }
                } else if (gotReadPermission == null) {
                    // We did not ask for read permission yet
                    if (observation.mediaAvailableWithoutReadPermission) {
                        observation.mediaState
                    } else {
                        _requestReadPermission?.let {
                            it()
                        }
                        null
                    }
                } else {
                    // We asked and we did not get read permission
                    observation.mediaState
                }
                MediaData(
                    remoteMedia, gotReadPermission, _startAudioOnPermissionResult,
                    observation.observation?.segmStart,
                    observation.observation?.segmEnd
                )
            }
        }

    companion object {
        fun updateFlow(
            initial: Flow<ObservationUpdate>,
            observationUpdate: Flow<ObservationUpdate>
        ): Flow<ObservationUpdate> {
            return merge(
                initial, observationUpdate
            ).runningReduce { acc: ObservationUpdate, e: ObservationUpdate ->
                acc.merge(e)
            }
        }
    }
}

class ObservationViewModelFactory(
    private val action: ObservationAction,
    private val application: Application
) : AbstractSavedStateViewModelFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T =
        ObservationViewModel(
            action,
            handle,
            application
        ) as T
}
