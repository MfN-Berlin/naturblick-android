package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "unambiguous_feature",
    primaryKeys = ["portrait_id", "description"],
    foreignKeys = [
        ForeignKey(
            entity = Portrait::class,
            parentColumns = ["rowid"],
            childColumns = ["portrait_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UnambiguousFeature(
    @ColumnInfo(name = "portrait_id") val portraitId: Int,
    @ColumnInfo(name = "description") val description: String
)
