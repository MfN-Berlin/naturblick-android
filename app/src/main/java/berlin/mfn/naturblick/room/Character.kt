package berlin.mfn.naturblick.room

import androidx.room.*
import berlin.mfn.naturblick.utils.isGerman

@Entity(
    tableName = "character"
)
data class Character(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "gername") val gername: String,
    @ColumnInfo(name = "engname") val engname: String,
    @ColumnInfo(name = "group") val group: String,
    @ColumnInfo(name = "weight") val weight: Int,
    @ColumnInfo(name = "single") val single: Boolean,
    @ColumnInfo(name = "gerdescription") val gerdescription: String?,
    @ColumnInfo(name = "engdescription") val engdescription: String?
) {
    val name
        get() = if (isGerman()) {
            gername
        } else {
            engname
        }
    val description
        get() = if (isGerman()) {
            gerdescription
        } else {
            engdescription
        }
}

data class CharacterWithValues(
    @Embedded val character: Character,
    @Relation(
        parentColumn = "rowid",
        entityColumn = "character_id"
    )
    val values: List<CharacterValue>
) {
    fun requiresUnset(toggled: Int): Set<Int> =
        if (character.single) {
            val valueIds = values.map { it.id }.toSet()
            if (valueIds.contains(toggled)) {
                valueIds.minus(toggled)
            } else {
                emptySet()
            }
        } else {
            emptySet()
        }
}
