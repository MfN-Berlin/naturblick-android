/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import berlin.mfn.naturblick.BuildConfig
import java.net.URL

@Entity(
    tableName = "portrait_image_size",
    primaryKeys = ["portrait_image_id", "width"],
    foreignKeys = [
        ForeignKey(
            entity = PortraitImage::class,
            parentColumns = ["rowid"],
            childColumns = ["portrait_image_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PortraitImageSize(
    @ColumnInfo(name = "portrait_image_id") val portraitImageId: Int,
    @ColumnInfo(name = "width") val width: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "url") val url: String
) {
    val fullUrl: String
        get() = BuildConfig.DJANGO_URL + url
}
