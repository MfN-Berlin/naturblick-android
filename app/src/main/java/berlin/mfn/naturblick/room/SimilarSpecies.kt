/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.*

data class FullSimilarSpecies(
    val species: Species,
    val portraitId: Int,
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
