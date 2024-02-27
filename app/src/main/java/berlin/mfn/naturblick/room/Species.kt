package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.utils.isGerman

@Entity(
    tableName = "species"
)
data class Species(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "group_id") val group: String,
    val sciname: String,
    val gername: String?,
    val engname: String?,
    val wikipedia: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "female_image_url") val femaleImageUrl: String?,
    val gersynonym: String?,
    val engsynonym: String?,
    @ColumnInfo(name = "red_list_germany") val redListGermany: String?,
    @ColumnInfo(name = "iucn_category") val iucnCategory: String?,
    @ColumnInfo(name = "old_species_id")val oldSpeciesId: String
) {
    val name
        get() = if (isGerman()) {
            gername
        } else {
            engname
        }
    val synonym
        get() = if (isGerman()) {
            gersynonym
        } else {
            engsynonym
        }

    val nameWithFallback: String get() = name ?: sciname

    val avatarUrl: String?
        get() = imageUrl?.let {
            "${BuildConfig.STRAPI_URL}$it"
        }

    val femaleAvatarUrl: String?
        get() = femaleImageUrl?.let {
            "${BuildConfig.STRAPI_URL}$it"
        }
}

data class SpeciesWithGenus(
    @Embedded() val species: Species,
    @ColumnInfo(name = "female") val female: Boolean?
) {
    val name: String
        get() = species.nameWithFallback + if (female != null) {
            if (female) {
                " ♀"
            } else {
                " ♂"
            }
        } else {
            ""
        }

    val avatarUrl: String?
        get() {
            val femaleAvatar = species.femaleAvatarUrl
            return if (female != null && female && femaleAvatar != null)
                femaleAvatar
            else
                species.avatarUrl
        }
}

data class SpeciesWithGroup(val id: Int, val group: String)
