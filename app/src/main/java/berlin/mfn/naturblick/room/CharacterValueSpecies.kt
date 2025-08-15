/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "character_value_species",
    foreignKeys = [
        ForeignKey(
            entity = CharacterValue::class,
            parentColumns = ["rowid"],
            childColumns = ["character_value_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Species::class,
            parentColumns = ["rowid"],
            childColumns = ["species_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("character_value_id"), Index("species_id")]
)
class CharacterValueSpecies(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "character_value_id") val characterValueId: Int,
    @ColumnInfo(name = "species_id") val speciesId: Int,
    @ColumnInfo(name = "weight") val weight: Int,
    @ColumnInfo(name = "female") val female: Boolean?
)
