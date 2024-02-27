package berlin.mfn.naturblick.room

import androidx.room.*

@DatabaseView("SELECT * FROM similar_species INNER JOIN species ON similar_to_id = rowid")
data class FullSimilarSpecies(
    @Embedded() val species: Species,
    @ColumnInfo(name = "portrait_id") val portraitId: Int,
    val differences: String
)

@Entity(
    tableName = "similar_species",
    primaryKeys = ["portrait_id", "similar_to_id"],
    indices = [Index("similar_to_id"), Index("portrait_id")],
    foreignKeys = [
        ForeignKey(
            entity = Portrait::class,
            parentColumns = ["rowid"],
            childColumns = ["portrait_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Species::class,
            parentColumns = ["rowid"],
            childColumns = ["similar_to_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SimilarSpecies(
    @ColumnInfo(name = "portrait_id") val portraitId: Int,
    @ColumnInfo(name = "similar_to_id") val similarToId: Int,
    val differences: String
)
