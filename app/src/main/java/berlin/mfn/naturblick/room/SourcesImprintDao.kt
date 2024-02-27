package berlin.mfn.naturblick.room

import androidx.room.*

@Entity(
    tableName = "sources_imprint"
)
data class SourcesImprint(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "scie_name") val scieName: String,
    @ColumnInfo(name = "scie_name_eng") val scieNameEng: String,
    @ColumnInfo(name = "image_source") val imageSource: String?,
    val licence: String?,
    val author: String?
)

@Dao
interface SourcesImprintDao {
    @Query("SELECT * FROM sources_imprint")
    suspend fun getSourcesImprint(): List<SourcesImprint>
}
