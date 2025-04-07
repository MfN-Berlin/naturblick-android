/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.*

data class FullPortrait(
    val portrait: Portrait,
    val description: ImageWithSizes?,
    val inTheCity: ImageWithSizes?,
    val goodToKnow: ImageWithSizes?,
    val similarSpecies: List<FullSimilarSpecies>,
    val unambiguousFeatures: List<UnambiguousFeature>,
    val goodToKnows: List<GoodToKnow>
) {
    val goodToKnowStrings: List<String> get() = goodToKnows.map { it.fact }
}

@Entity(
    tableName = "portrait", indices = [Index("species_id")], foreignKeys = [ForeignKey(
        entity = Species::class,
        parentColumns = ["rowid"],
        childColumns = ["species_id"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = PortraitImage::class,
        parentColumns = ["rowid"],
        childColumns = ["description_image_id"],
        onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = PortraitImage::class,
        parentColumns = ["rowid"],
        childColumns = ["in_the_city_image_id"],
        onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = PortraitImage::class,
        parentColumns = ["rowid"],
        childColumns = ["good_to_know_image_id"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Portrait(
    @PrimaryKey @ColumnInfo(name = "rowid") val id: Int,
    @ColumnInfo(name = "species_id") val speciesId: Int,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "description_image_id") val descriptionImageId: Int?,
    @ColumnInfo(name = "language") val language: Int,
    @ColumnInfo(name = "in_the_city") val inTheCity: String,
    @ColumnInfo(name = "in_the_city_image_id") val inTheCityImageId: Int?,
    @ColumnInfo(name = "good_to_know_image_id") val goodToKnowImageId: Int?,
    @ColumnInfo(name = "sources") val sources: String?,
    @ColumnInfo(name = "audio_url") val audioUrl: String?,
    @ColumnInfo(name = "landscape") val landscape: Boolean,
    @ColumnInfo(name = "focus") val focus: Float
)
