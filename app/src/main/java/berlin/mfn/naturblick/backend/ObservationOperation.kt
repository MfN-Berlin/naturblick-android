/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

@file:UseSerializers(UUIDSerializer::class, ZonedDateTimeSerializer::class)

package berlin.mfn.naturblick.backend

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import berlin.mfn.naturblick.utils.MediaType
import berlin.mfn.naturblick.utils.ObservationOperationSerializer
import berlin.mfn.naturblick.utils.UUIDSerializer
import berlin.mfn.naturblick.utils.ZonedDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import java.time.ZonedDateTime
import java.util.UUID

@Serializable()
enum class ObsType {
    @SerialName("manual")
    MANUAL,

    @SerialName("audio")
    AUDIO,

    @SerialName("image")
    IMAGE,

    @SerialName("unidentifiedimage")
    UNIDENTIFIED_IMAGE,

    @SerialName("unidentifiedaudio")
    UNIDENTIFIED_AUDIO
}

@Serializable(with = ObservationOperationSerializer::class)
sealed class ObservationOperation {
    abstract val operationId: Long
}

@Entity(
    tableName = "create_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable()
data class CreateOperation(
    @ColumnInfo(name = "occurence_id") val occurenceId: UUID,
    @ColumnInfo(name = "created") val created: ZonedDateTime,
    @ColumnInfo(name = "species_id") val speciesId: Int?,
    @ColumnInfo(name = "cc_by_name") val ccByName: String,
    @ColumnInfo(name = "obs_type") val obsType: ObsType,
    @ColumnInfo(name = "media_id") val mediaId: UUID? = null,
    @ColumnInfo(name = "thumbnail_id") val thumbnailId: UUID? = null,
    @ColumnInfo(name = "segm_start") val segmStart: Int? = null,
    @ColumnInfo(name = "segm_end") val segmEnd: Int? = null,
    @ColumnInfo(name = "device_identifier") val deviceIdentifier: String,
    @ColumnInfo(name = "app_version") val appVersion: String,
    @ColumnInfo(name = "imported", defaultValue = "FALSE") val imported: Boolean,
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(
    tableName = "patch_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable()
data class PatchOperation(
    @ColumnInfo(name = "occurence_id") val occurenceId: UUID,
    @ColumnInfo(name = "species_id") val newSpeciesId: Int? = null,
    @Embedded(prefix = "coordinate_") val coords: Coordinates? = null,
    @ColumnInfo(name = "details") val details: String? = null,
    @ColumnInfo(name = "behavior") val behavior: String? = null,
    @ColumnInfo(name = "individuals") val individuals: Int? = null,
    @ColumnInfo(name = "media_id") val mediaId: UUID? = null,
    @ColumnInfo(name = "thumbnail_id") val thumbnailId: UUID? = null,
    @ColumnInfo(name = "segm_start") val segmStart: Int? = null,
    @ColumnInfo(name = "segm_end") val segmEnd: Int? = null,
    @ColumnInfo(name = "local_media_id") val localMediaId: String? = null,
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(
    tableName = "upload_media_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable
data class UploadMediaOperation(
    @ColumnInfo(name = "media_id") val mediaId: UUID,
    @Transient @ColumnInfo(name = "filename") val filename: String = "",
    @Transient @ColumnInfo(name = "mime") val mime: String = MediaType.JPG.mime,
    @Transient @ColumnInfo(name = "uploaded", defaultValue = "false") val uploaded: Boolean = false,
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(
    tableName = "upload_thumbnail_media_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable
data class UploadThumbnailMediaOperation(
    @ColumnInfo(name = "media_id") val mediaId: UUID,
    @Transient @ColumnInfo(name = "filename") val filename: String = "",
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(
    tableName = "delete_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable()
data class DeleteOperation(
    @ColumnInfo(name = "occurence_id") val occurenceId: UUID,
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(
    tableName = "view_fieldbook_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable()
data class ViewFieldbookOperation(
    @ColumnInfo(name = "device_identifier") val deviceIdentifier: String,
    @ColumnInfo(name = "timestamp") val timestamp: ZonedDateTime,
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(
    tableName = "view_portrait_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable()
data class ViewPortraitOperation(
    @ColumnInfo(name = "device_identifier") val deviceIdentifier: String,
    @ColumnInfo(name = "species_id") val speciesId: Int,
    @ColumnInfo(name = "timestamp") val timestamp: ZonedDateTime,
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(
    tableName = "view_characters_operation",
    foreignKeys = [
        ForeignKey(
            entity = ObservationOperationEntry::class,
            parentColumns = ["rowid"],
            childColumns = ["operation_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@Serializable()
data class ViewCharactersOperation(
    @ColumnInfo(name = "device_identifier") val deviceIdentifier: String,
    @ColumnInfo(name = "group") val group: String,
    @ColumnInfo(name = "timestamp") val timestamp: ZonedDateTime,
    @Transient @PrimaryKey @ColumnInfo(name = "operation_id") override val operationId: Long = -1
) : ObservationOperation()

@Entity(tableName = "operation")
data class ObservationOperationEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int
)


data class ObservationOperationEntryWithOperation(
    @Embedded val operation: ObservationOperationEntry,
    @Relation(parentColumn = "rowid", entityColumn = "operation_id") val create: CreateOperation?,
    @Relation(parentColumn = "rowid", entityColumn = "operation_id") val patch: PatchOperation?,
    @Relation(parentColumn = "rowid", entityColumn = "operation_id") val delete: DeleteOperation?,
    @Relation(
        parentColumn = "rowid",
        entityColumn = "operation_id"
    ) val upload: UploadMediaOperation?,
    @Relation(
        parentColumn = "rowid",
        entityColumn = "operation_id"
    ) val uploadThumbnail: UploadThumbnailMediaOperation?,
    @Relation(
        parentColumn = "rowid",
        entityColumn = "operation_id"
    ) val viewFieldbook: ViewFieldbookOperation?,
    @Relation(
        parentColumn = "rowid",
        entityColumn = "operation_id"
    ) val viewPortrait: ViewPortraitOperation?,
    @Relation(
        parentColumn = "rowid",
        entityColumn = "operation_id"
    ) val viewCharacters: ViewCharactersOperation?
)
