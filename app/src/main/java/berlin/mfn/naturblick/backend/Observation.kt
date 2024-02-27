@file:UseSerializers(UUIDSerializer::class, ZonedDateTimeSerializer::class)

package berlin.mfn.naturblick.backend

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import berlin.mfn.naturblick.utils.UUIDSerializer
import berlin.mfn.naturblick.utils.ZonedDateTimeSerializer
import java.time.ZonedDateTime
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

/**
 * OBSERVE (2021-12-14):
 * The two following classes are and must be identical. The only reason they are not a single class
 * is because there needs to be two tables in the DB. One stores the original observations from
 * the backend (backend_observations) and one stores the locally updated snapshot (observations).
 * A solution where single class is used for both tables is currently not possible with room.
 * A solution where both classes inherit from a single base class is currently problematic:
 * - @ColumnInfo and friends are only allowed for concrete class members (i.e. val or var) which
 *   prevents an interface to be used as a base
 * - @Serializable does not accept any non concrete constructor arguments which prevents a concrete
 *   base class to be used
 *
 * If you find a solution to this problem, please update the code and remove this comment!
 */

@Entity(tableName = "backend_observation")
@Serializable
data class BackendObservation(
    @PrimaryKey @ColumnInfo(name = "occurence_id") val occurenceId: UUID,
    @ColumnInfo(name = "obs_ident") val obsIdent: String?,
    @ColumnInfo(name = "obs_type") val obsType: ObsType,
    @ColumnInfo(name = "species_id") val newSpeciesId: Int? = null,
    @ColumnInfo(name = "created") val created: ZonedDateTime,
    @ColumnInfo(name = "media_id") val mediaId: UUID? = null,
    @ColumnInfo(name = "thumbnail_id") val thumbnailId: UUID? = null,
    @ColumnInfo(name = "local_media_id") val localMediaId: String? = null,
    @Embedded(prefix = "coordinates_") val coords: Coordinates? = null,
    @ColumnInfo(name = "individuals") val individuals: Int? = null,
    @ColumnInfo(name = "behavior") val behavior: String? = null,
    @ColumnInfo(name = "details") val details: String? = null,
    @ColumnInfo(name = "segm_start") val segmStart: Int? = null,
    @ColumnInfo(name = "segm_end") val segmEnd: Int? = null
)

@Entity(tableName = "observation")
data class Observation(
    @PrimaryKey @ColumnInfo(name = "occurence_id") val occurenceId: UUID,
    @ColumnInfo(name = "obs_ident") val obsIdent: String?,
    @ColumnInfo(name = "obs_type") val obsType: ObsType,
    @ColumnInfo(name = "species_id") val newSpeciesId: Int?,
    @ColumnInfo(name = "created") val created: ZonedDateTime,
    @ColumnInfo(name = "media_id") val mediaId: UUID?,
    @ColumnInfo(name = "thumbnail_id") val thumbnailId: UUID?,
    @ColumnInfo(name = "local_media_id") val localMediaId: String?,
    @Embedded(prefix = "coordinates_") val coords: Coordinates?,
    @ColumnInfo(name = "individuals") val individuals: Int?,
    @ColumnInfo(name = "behavior") val behavior: String?,
    @ColumnInfo(name = "details") val details: String?,
    @ColumnInfo(name = "segm_start") val segmStart: Int? = null,
    @ColumnInfo(name = "segm_end") val segmEnd: Int? = null
)
