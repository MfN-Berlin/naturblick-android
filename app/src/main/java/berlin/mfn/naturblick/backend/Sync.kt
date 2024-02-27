package berlin.mfn.naturblick.backend

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync")
data class Sync(
    @PrimaryKey @ColumnInfo(name = "sync_id") val syncId: Long,
)
