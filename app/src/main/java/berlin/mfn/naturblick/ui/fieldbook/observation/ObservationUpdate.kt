/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.observation

import android.content.Context
import berlin.mfn.naturblick.backend.*
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.ui.fieldbook.fieldbook.FieldbookObservation
import berlin.mfn.naturblick.utils.*
import java.text.NumberFormat
import java.time.ZonedDateTime
import java.util.*

enum class Action {
    SET, CLEAR, NOTHING
}

data class ObservationUpdate(
    val observation: Observation? = null,
    val created: ZonedDateTime? = null,
    val details: String? = null,
    val individuals: Int? = null,
    val behavior: String? = null,
    val speciesId: Int? = null,
    val obsType: ObsType? = null,
    val media: Media? = null,
    val thumbnail: MediaThumbnail? = null,
    val coordinates: Coordinates? = null,
    val reset: Boolean = false
) {
    val createdState: ZonedDateTime? = created ?: observation?.created
    val detailsState: String? = details ?: observation?.details
    val behaviorState: String? = behavior ?: observation?.behavior
    val individualsState: Int? = individuals ?: observation?.individuals
    val speciesIdState: Int? = speciesId ?: observation?.newSpeciesId
    val obsTypeState: ObsType = (obsType ?: observation?.obsType) ?: ObsType.MANUAL
    private val mediaType = when (obsTypeState) {
        ObsType.MANUAL -> null
        ObsType.IMAGE -> MediaType.JPG
        ObsType.UNIDENTIFIED_IMAGE -> MediaType.JPG
        ObsType.AUDIO -> MediaType.MP4
        ObsType.UNIDENTIFIED_AUDIO -> MediaType.MP4
    }
    val coordinatesState: Coordinates? = coordinates ?: observation?.coords
    val mediaState: Media? = media ?: observation?.mediaId?.let {
        mediaType?.let { mt ->
            when (mt) {
                MediaType.JPG ->
                    RemoteMedia(
                        MediaType.JPG,
                        it,
                        ExistingMedia,
                        observation.localMediaId,
                        observation.obsIdent
                    )
                MediaType.MP4 ->
                    RemoteMedia(
                        MediaType.MP4,
                        it,
                        ExistingMedia,
                        observation.localMediaId,
                        observation.obsIdent
                    )
            }
        }
    }
    val thumbnailState: MediaThumbnail? = thumbnail ?: observation?.thumbnailId?.let {
        MediaThumbnail.remote(it, observation.obsIdent)
    }

    val mediaAvailableWithoutReadPermission: Boolean =
        observation == null || // New observations never require extra permissions
            obsTypeState == ObsType.MANUAL || // Manual observations as well
            mediaState != null // Or if we have a media id

    fun hasChanges(): Boolean =
        details != null ||
            individuals != null ||
            speciesId != null ||
            media != null ||
            thumbnail != null ||
            coordinates != null ||
            behavior != null

    fun isNew(): Boolean =
        observation == null

    fun isEmpty(): Boolean =
        isNew() && !hasChanges()

    fun isImage(): Boolean =
        obsTypeState == ObsType.IMAGE || obsTypeState == ObsType.UNIDENTIFIED_IMAGE

    fun hasCoordinates(): Boolean =
        coordinatesState != null

    private fun <T> action(original: T?, update: T?): Action =
        if (original != null && update != null && original == update) {
            Action.CLEAR // Update is identical to existing value in observation
        } else if (update != null) {
            Action.SET // Field was updated but is not identical to value in observation
        } else {
            Action.NOTHING // Field was not updated
        }

    fun merge(update: ObservationUpdate): ObservationUpdate = if (update.reset) {
        update.copy(reset = false)
    } else {
        run {
            if (update.observation != null)
            // Set observation and then update self with all existing fields to clear them if same
                copy(observation = update.observation)
                    .merge(copy(observation = null))
            else
                this
        }.run {
            when (action(observation?.created, update.created)) {
                Action.SET -> copy(created = update.created)
                Action.CLEAR -> copy(created = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.details, update.details)) {
                Action.SET -> copy(details = update.details)
                Action.CLEAR -> copy(details = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.individuals, update.individuals)) {
                Action.SET -> copy(individuals = update.individuals)
                Action.CLEAR -> copy(individuals = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.newSpeciesId, update.speciesId)) {
                Action.SET -> copy(speciesId = update.speciesId)
                Action.CLEAR -> copy(speciesId = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.obsType, update.obsType)) {
                Action.SET -> copy(obsType = update.obsType)
                Action.CLEAR -> copy(obsType = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.mediaId, update.media?.id)) {
                Action.SET -> copy(media = update.media)
                Action.CLEAR -> copy(media = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.thumbnailId, update.thumbnail?.id)) {
                Action.SET -> copy(thumbnail = update.thumbnail)
                Action.CLEAR -> copy(thumbnail = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.coords, update.coordinates)) {
                Action.SET -> copy(coordinates = update.coordinates)
                Action.CLEAR -> copy(coordinates = null)
                Action.NOTHING -> this
            }
        }.run {
            when (action(observation?.behavior, update.behavior)) {
                Action.SET -> copy(behavior = update.behavior)
                Action.CLEAR -> copy(behavior = null)
                Action.NOTHING -> this
            }
        }
    }

    private suspend fun toPatchOperation(
        context: Context,
        occurenceId: UUID
    ): PatchOperation? =
        if (details == null &&
            individuals == null &&
            speciesId == null &&
            media == null &&
            coordinates == null &&
            thumbnail == null &&
            behavior == null
        ) {
            null
        } else {
            PatchOperation(
                occurenceId = occurenceId,
                details = details,
                individuals = individuals,
                newSpeciesId = speciesId,
                mediaId = media?.syncRemote(context)?.id,
                thumbnailId = thumbnail?.syncRemote(context)?.id,
                coords = coordinates,
                behavior = behavior?.let {
                    Behavior.parse(context, it)?.value
                }
            )
        }

    suspend fun toOperations(
        context: Context,
        occurenceId: UUID,
        ccBy: String,
        appVersion: String,
        deviceIdentifier: String
    ): List<ObservationOperation> {
        return if (observation != null) {
            listOfNotNull(toPatchOperation(context, occurenceId))
        } else {
            listOfNotNull(
                CreateOperation(
                    occurenceId,
                    createdState ?: ZonedDateTime.now(),
                    obsType = obsTypeState,
                    speciesId = speciesId,
                    ccByName = ccBy,
                    appVersion = appVersion,
                    deviceIdentifier = deviceIdentifier,
                    imported = media?.meta is ImportedMediaMeta
                ),
                // Species already set by create
                copy(speciesId = null).toPatchOperation(context, occurenceId)
            )
        }
    }

    fun toFieldbookObservation(
        occurenceId: UUID,
        species: Species?
    ): FieldbookObservation =
        FieldbookObservation(
            occurenceId,
            createdState ?: ZonedDateTime.now(),
            thumbnail ?: observation?.thumbnailId?.let {
                MediaThumbnail.remote(it, observation.obsIdent)
            },
            observation?.obsIdent,
            species,
            coordinatesState
        )

    val longitudeString: String?
        get() = coordinatesState?.lon?.let {
            NumberFormat.getNumberInstance().format(it)
        }
    val latitudeString: String?
        get() = coordinatesState?.lat?.let {
            NumberFormat.getNumberInstance().format(it)
        }

    companion object {
        val EMPTY = ObservationUpdate()
    }
}
