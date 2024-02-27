package berlin.mfn.naturblick.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TimeZonePolygonDao {
    @Query("SELECT * FROM time_zone_vertex ORDER BY rowid")
    suspend fun getTimeZoneVertices(): List<TimeZoneVertex>

    @Query("SELECT * FROM time_zone_polygon WHERE rowid = :zoneId")
    suspend fun getTimeZonePolygon(zoneId: Int): TimeZonePolygon
}
