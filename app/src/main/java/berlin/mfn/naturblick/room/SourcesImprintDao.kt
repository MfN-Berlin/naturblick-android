/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "sources_imprint"
)
data class SourcesImprint(
    @PrimaryKey val id: Int,
    val section: String,
    @ColumnInfo(name = "scie_name") val scieName: String,
    @ColumnInfo(name = "scie_name_eng") val scieNameEng: String,
    @ColumnInfo(name = "image_source") val imageSource: String?,
    val licence: String?,
    val author: String?
)


@Dao
interface SourcesImprintDao {
    @Query("SELECT * FROM sources_imprint")
    fun getSourcesImprint(): Flow<List<SourcesImprint>>
}
