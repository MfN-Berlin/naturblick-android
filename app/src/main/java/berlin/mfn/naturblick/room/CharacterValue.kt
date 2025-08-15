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
import berlin.mfn.naturblick.utils.isGerman

@Entity(
    tableName = "character_value",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["rowid"],
            childColumns = ["character_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("character_id")]
)
class CharacterValue(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "character_id") val characterId: Int,
    @ColumnInfo(name = "gername") val gername: String,
    @ColumnInfo(name = "engname") val engname: String,
    @ColumnInfo(name = "has_image") val hasImage: Boolean
) {
    val name
        get() = if (isGerman()) {
            gername
        } else {
            engname
        }
}
