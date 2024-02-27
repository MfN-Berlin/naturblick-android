package berlin.mfn.naturblick.room

import androidx.room.*

@DatabaseView(
    "SELECT * FROM portrait_image INNER JOIN portrait_image_size ON portrait_image_id = rowid"
)
data class ImageWithSizes(
    @Embedded val image: PortraitImage,
    @Relation(parentColumn = "rowid", entityColumn = "portrait_image_id")
    val sizes: List<PortraitImageSize>
) {
    val sorted: List<PortraitImageSize> get() = sizes.sortedBy {
        it.width
    }
    val ratio: String get() = sorted.lastOrNull()?.let {
        "H,${it.width}:${it.height}"
    } ?: "H,1:1"

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
