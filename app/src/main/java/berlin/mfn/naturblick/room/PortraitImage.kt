/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.*

data class ImageWithSizes(val image: PortraitImage, val sizes: List<PortraitImageSize>) {
    val sorted: List<PortraitImageSize>
        get() = sizes.sortedBy {
            it.width
        }
    val ratio: String
        get() = sorted.lastOrNull()?.let {
            "H,${it.width}:${it.height}"
        } ?: "H,1:1"

    val largest: PortraitImageSize?
        get() = sizes.maxByOrNull { it.width * it.height }

    fun widerThanFocusPoint(landscape: Boolean): Boolean = sorted.lastOrNull()?.let {
        if (landscape) {
            it.width.toFloat() / it.height.toFloat() > 4f / 3f
        } else {
            it.width.toFloat() / it.height.toFloat() > 3f / 4f
        }
    } ?: false
}

@Entity(
    tableName = "portrait_image",
)
data class PortraitImage(
    @PrimaryKey() @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "owner") val owner: String,
    @ColumnInfo(name = "owner_link") val ownerLink: String?,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "license") val license: String
)
