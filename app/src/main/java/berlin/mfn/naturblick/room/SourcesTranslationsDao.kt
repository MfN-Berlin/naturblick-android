package berlin.mfn.naturblick.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query

@Entity(
    tableName = "sources_translations",
    primaryKeys = ["language", "key"]
)
data class SourcesTranslations(
    val language: Int,
    val key: String,
    val value: String
)

@Dao
interface SourcesTranslationsDao {
    @Query("SELECT * FROM sources_translations WHERE language = :language")
    suspend fun getSourcesTranslations(language: Int): List<SourcesTranslations>
}
