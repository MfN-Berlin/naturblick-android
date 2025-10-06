/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.utils.Wikipedia
import berlin.mfn.naturblick.utils.isGerman

@Entity(
    tableName = "species",
    indices = [
        Index(value = ["gername"], name = "idx_species_gername"),
        Index(value = ["engname"], name = "idx_species_engname")
    ],
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["name"],
            childColumns = ["group_id"]
        )
    ]
)
data class Species(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "group_id") val group: String,
    val sciname: String,
    val gername: String?,
    val engname: String?,
    val wikipedia: String?,
    @ColumnInfo(name = "image_url") val imageUrl: String?,
    @ColumnInfo(name = "image_url_orig") val imageUrlOrig: String?,
    @ColumnInfo(name = "image_url_owner") val imageUrlOwner: String?,
    @ColumnInfo(name = "image_url_owner_link") val imageUrlOwnerLink: String?,
    @ColumnInfo(name = "image_url_source") val imageUrlSource: String?,
    @ColumnInfo(name = "image_url_license") val imageUrlLicense: String?,
    @ColumnInfo(name = "female_image_url") val femaleImageUrl: String?,
    val gersynonym: String?,
    val engsynonym: String?,
    @ColumnInfo(name = "red_list_germany") val redListGermany: String?,
    @ColumnInfo(name = "iucn_category") val iucnCategory: String?,
    @ColumnInfo(name = "old_species_id") val oldSpeciesId: String,
    @ColumnInfo(name = "accepted") val accepted: Int?,
    @ColumnInfo(name = "gersearchfield") val gersearchfield: String?,
    @ColumnInfo(name = "engsearchfield") val engsearchfield: String?
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
            "${BuildConfig.DJANGO_URL}$it"
        }

    val avatarOrigUrl: String?
        get() = imageUrlOrig?.let {
            "${BuildConfig.DJANGO_URL}$it"
        }

    val femaleAvatarUrl: String?
        get() = femaleImageUrl?.let {
            "${BuildConfig.DJANGO_URL}$it"
        }

    val wikipediaUri: Uri?
        get() = Wikipedia.uri(this)

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
