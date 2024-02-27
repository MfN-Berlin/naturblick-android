package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "good_to_know",
    primaryKeys = ["portrait_id", "fact"],
    foreignKeys = [
        ForeignKey(
            entity = Portrait::class,
            parentColumns = ["rowid"],
            childColumns = ["portrait_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GoodToKnow(
    @ColumnInfo(name = "portrait_id") val portraitId: Int,
    @ColumnInfo(name = "fact") val fact: String
)
