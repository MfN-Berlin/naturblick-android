/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "time_zone_vertex",
    foreignKeys = [
        ForeignKey(
            entity = TimeZonePolygon::class,
            parentColumns = ["rowid"],
            childColumns = ["polygon_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TimeZoneVertex(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "polygon_id") val polygonId: Int,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double
)
