package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_zone_polygon"
)
data class TimeZonePolygon(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "zone_id") val zoneId: String
)
