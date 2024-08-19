/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import android.content.Context
import android.os.Parcelable
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.room.TimeZonePolygonDao
import berlin.mfn.naturblick.room.TimeZoneVertex
import berlin.mfn.naturblick.utils.CoordinatesSerializer
import com.mapbox.geojson.Point
import java.time.ZoneId
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable(with = CoordinatesSerializer::class)
data class Coordinates(val lat: Double, val lon: Double) : Parcelable {
    fun toPoint(): Point {
        return Point.fromLngLat(lon, lat)
    }

    suspend fun getTimeZone(context: Context): ZoneId? {
        val dao = StrapiDb.getDb(context).timeZonePolygonDao()
        return getPolygons(dao)
            .filter { (_, polygon) ->
                insideOf(polygon)
            }.map { (dbZoneId, _) ->
                val zoneIdStr = dao.getTimeZonePolygon(dbZoneId).zoneId
                ZoneId.of(zoneIdStr)
            }.firstOrNull()
    }

    /*
    * Based on: https://github.com/piruin/geok/blob/master/geok/src/commonMain/kotlin/me/piruin/geok/LatLng.kt#L122
    */
    private fun insideOf(polygon: List<TimeZoneVertex>): Boolean {
        var i = 0
        var j = polygon.size - 1
        var result = false

        while (i < polygon.size) {
            val u = (polygon[j].longitude - polygon[i].longitude) *
                (this.lat - polygon[i].latitude) /
                (polygon[j].latitude - polygon[i].latitude) +
                polygon[i].longitude
            if (
                polygon[i].latitude > this.lat != polygon[j].latitude > this.lat &&
                this.lon < u
            ) result = !result
            j = i++
        }
        return result
    }

    private suspend fun getPolygons(dao: TimeZonePolygonDao): Map<Int, List<TimeZoneVertex>> =
        dao.getTimeZoneVertices().groupBy {
            it.polygonId
        }
}
