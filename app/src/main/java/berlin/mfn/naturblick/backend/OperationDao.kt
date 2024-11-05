/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import berlin.mfn.naturblick.utils.Media
import berlin.mfn.naturblick.utils.MediaType
import berlin.mfn.naturblick.utils.NetworkResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import java.io.File
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Dao
interface OperationDao {

    /**
     * @return all pending operations.
     */
    suspend fun getOperations(): List<ObservationOperation> {
        return getOperationEntries().map {
            when {
                it.create != null -> {
                    it.create
                }
                it.patch != null -> {
                    it.patch
                }
                it.delete != null -> {
                    it.delete
                }
                it.upload != null -> {
                    it.upload
                }
                it.uploadThumbnail != null -> {
                    it.uploadThumbnail
                }
                else -> {
                    error("An operation must have create, patch, upload or delete set")
                }
            }
        }
    }

    /**
     * @return LiveData for all observations
     */
    @Query("SELECT * FROM observation ORDER BY created DESC")
    fun getAllObservations(): Flow<List<Observation>>

    /**
     * @return LiveData for all observations
     */
    @Query("SELECT * FROM observation ORDER BY created DESC")
    suspend fun getMapObservations(): List<Observation>

    /**
     * @return LiveData for an observation in the current snapshot
     */
    @Query("SELECT * FROM observation WHERE occurence_id = :occurenceId")
    fun getObservationFlow(occurenceId: UUID?): Flow<Observation?>

    @Query("SELECT * FROM observation WHERE occurence_id = :occurenceId")
    suspend fun getObservation(occurenceId: UUID): Observation

    @Query("SELECT * FROM upload_media_operation WHERE media_id = :mediaId")
    suspend fun getUploadOperation(mediaId: UUID): UploadMediaOperation?

    suspend fun wasUploaded(mediaId: UUID): Boolean =
        getUploadOperation(mediaId)?.uploaded ?: true

    @Query("UPDATE upload_media_operation SET uploaded = 1 WHERE media_id = :mediaId")
    suspend fun setUploaded(mediaId: UUID)

    suspend fun insertOperationsAndRefresh(vararg operation: ObservationOperation): List<Long> {
        val ids = operation.map { insertOperation(it) }
        refreshObservations()
        return ids
    }

    /**
     * Enqueue an operation to be applied to the observations state
     * @return the operation ID given by the DB
     */
    @Transaction
    suspend fun insertOperation(operation: ObservationOperation): Long {
        val operationId = insertOperationEntry(ObservationOperationEntry(0))
        when (operation) {
            is PatchOperation -> insertPatchOperation(operation.copy(operationId = operationId))
            is DeleteOperation -> insertDeleteOperation(operation.copy(operationId = operationId))
            is CreateOperation -> insertCreateOperation(operation.copy(operationId = operationId))
            is UploadMediaOperation ->
                insertUploadMediaOperation(operation.copy(operationId = operationId))
            is UploadThumbnailMediaOperation ->
                insertUploadThumbnailMediaOperation(operation.copy(operationId = operationId))
        }
        return operationId
    }

    private suspend fun jpegCopy(context: Context, local: Uri, upload: File): File? =
        withContext(Dispatchers.IO) {
            NetworkResult.ioToFileException {
                Glide
                    .with(context)
                    .load(local)
                    .centerInside()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .submit(
                        PublicBackendApiService.MAX_RESOLUTION,
                        PublicBackendApiService.MAX_RESOLUTION
                    ).get()?.let { drawable ->
                        val image = (drawable as BitmapDrawable).bitmap

                        upload.outputStream().use { os ->
                            image.compress(Bitmap.CompressFormat.JPEG, Media.JPEG_QUALITY, os)
                        }
                        upload
                    }
            }
        }

    private suspend fun mp4Copy(context: Context, local: Uri, upload: File): File? {
        return withContext(Dispatchers.IO) {
            NetworkResult.ioToFileException {
                context.contentResolver.openInputStream(local)?.buffered()?.use {
                    val byteArray = it.readBytes()
                    upload.outputStream().use { os ->
                        os.write(byteArray)
                    }
                    upload
                }
            }
        }
    }

    @Transaction
    suspend fun upsertMedia(
        context: Context,
        mediaId: UUID,
        type: MediaType,
        local: Uri,
        upload: File
    ): Long {
        val existing = findUploadMediaOperationByMediaId(mediaId)
        return existing?.operationId
            ?: if (when (type) {
                MediaType.JPG ->
                    jpegCopy(context, local, upload)
                MediaType.MP4 ->
                    mp4Copy(context, local, upload)
            } != null
            ) {
                val operationId = insertOperationEntry(ObservationOperationEntry(0))
                insertUploadMediaOperation(
                    UploadMediaOperation(
                        mediaId = mediaId,
                        filename = upload.name,
                        mime = type.mime,
                        operationId = operationId
                    )
                )
                operationId
            } else {
                throw IllegalStateException("Failed to find original media to copy")
            }
    }

    @Transaction
    suspend fun upsertThumbnail(operation: UploadThumbnailMediaOperation): Long {
        val existing = findUploadThumbnailMediaOperationByMediaId(operation.mediaId)
        return if (existing == null) {
            val operationId = insertOperationEntry(ObservationOperationEntry(0))
            insertUploadThumbnailMediaOperation(operation.copy(operationId = operationId))
            operationId
        } else {
            operation.operationId
        }
    }

    /**
     * Combine all observations from the backend with all pending operations and store the result
     * in the observation state.
     */
    @Transaction
    suspend fun refreshObservations() {
        deleteAllObservations()
        copyFromBackend()
        getOperations().forEach {
            when (it) {
                is PatchOperation -> patchObservation(
                    it.occurenceId,
                    it.newSpeciesId,
                    it.coords?.lat,
                    it.coords?.lon,
                    it.details,
                    it.behavior,
                    it.individuals,
                    it.mediaId,
                    it.thumbnailId,
                    it.localMediaId
                )
                is DeleteOperation -> deleteObservation(it.occurenceId)
                is CreateOperation -> createObservation(
                    Observation(
                        it.occurenceId,
                        null,
                        it.obsType,
                        it.speciesId,
                        it.created,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                )
                is UploadMediaOperation -> {}
                is UploadThumbnailMediaOperation -> {}
            }
        }
    }

    /**
     * Update database with new observations from backend and optionally remove any operations
     * that were synced.
     */
    @Transaction
    suspend fun updateBackendObservations(
        observations: List<BackendObservation>,
        partial: Boolean,
        executedOperations: List<Long>
    ) {
        if (!partial) deleteAllBackendObservations()
        insertBackendObservations(observations)
        deleteOperations(executedOperations)
    }

    /* All methods below are only used internally by this class,
     * but since they are autogenerated, they still have to be public
     */

    @Query("DELETE FROM observation")
    suspend fun deleteAllObservations()

    @Query("DELETE FROM backend_observation")
    suspend fun deleteAllBackendObservations()

    @Insert(onConflict = REPLACE)
    suspend fun insertBackendObservations(observations: List<BackendObservation>)

    @Query("INSERT INTO observation SELECT * FROM backend_observation")
    suspend fun copyFromBackend()

    @Query(
        """UPDATE observation
        SET species_id =
            CASE WHEN :newSpeciesId IS NOT NULL THEN :newSpeciesId ELSE species_id END,
        coordinates_lat =
            CASE WHEN :lat IS NOT NULL THEN :lat ELSE coordinates_lat END,
        coordinates_lon =
            CASE WHEN :lon IS NOT NULL THEN :lon ELSE coordinates_lon END,
        details =
            CASE WHEN :details IS NOT NULL THEN :details ELSE details END,
        behavior =
            CASE WHEN :behavior IS NOT NULL THEN :behavior ELSE behavior END,
        individuals =
            CASE WHEN :individuals IS NOT NULL THEN :individuals ELSE individuals END,
        media_id = 
            CASE WHEN :mediaId IS NOT NULL THEN :mediaId ELSE media_id END,
        thumbnail_id = 
            CASE WHEN :thumbnailId IS NOT NULL THEN :thumbnailId ELSE thumbnail_id END,
        local_media_id = 
            CASE WHEN :localMediaId IS NOT NULL THEN :localMediaId ELSE local_media_id END
     WHERE occurence_id = :occurenceId"""
    )
    suspend fun patchObservation(
        occurenceId: UUID,
        newSpeciesId: Int?,
        lat: Double?,
        lon: Double?,
        details: String?,
        behavior: String?,
        individuals: Int?,
        mediaId: UUID?,
        thumbnailId: UUID?,
        localMediaId: String?
    )

    @Query("DELETE from observation WHERE occurence_id = :occurenceId")
    suspend fun deleteObservation(occurenceId: UUID)

    @Insert(onConflict = REPLACE)
    suspend fun createObservation(observation: Observation)

    @Query("DELETE FROM operation")
    suspend fun deleteAll()

    @Insert
    suspend fun insertOperationEntry(operationEntry: ObservationOperationEntry): Long

    @Insert
    suspend fun insertPatchOperation(patch: PatchOperation)

    @Insert
    suspend fun insertDeleteOperation(delete: DeleteOperation)

    @Insert
    suspend fun insertCreateOperation(create: CreateOperation)

    @Insert
    suspend fun insertUploadMediaOperation(uploadMedia: UploadMediaOperation)

    @Insert
    suspend fun insertUploadThumbnailMediaOperation(uploadMedia: UploadThumbnailMediaOperation)

    @Query("SELECT * FROM upload_thumbnail_media_operation WHERE media_id = :mediaId")
    suspend fun findUploadThumbnailMediaOperationByMediaId(mediaId: UUID):
        UploadThumbnailMediaOperation?

    @Query("SELECT * FROM upload_media_operation WHERE media_id = :mediaId")
    suspend fun findUploadMediaOperationByMediaId(mediaId: UUID):
        UploadMediaOperation?

    @Transaction
    @Query("SELECT * FROM operation ORDER BY rowid ASC")
    suspend fun getOperationEntries(): List<ObservationOperationEntryWithOperation>

    @Query("DELETE FROM operation WHERE rowid in (:successfulOperations)")
    suspend fun deleteOperations(successfulOperations: List<Long>)
}
