/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.*

@Dao
interface PortraitDao {
    @Query("SELECT * FROM portrait WHERE species_id = :speciesId AND language = :language")
    suspend fun getPortrait(speciesId: Int, language: Int): Portrait?

    @Query("SELECT * FROM portrait_image WHERE rowid = :portraitImageId")
    suspend fun getPortraitImage(portraitImageId: Int): PortraitImage?

    @Query("SELECT * FROM portrait_image_size WHERE portrait_image_id = :portraitImageId")
    suspend fun getPortraitImageSizes(portraitImageId: Int): List<PortraitImageSize>

    @Query("SELECT * FROM unambiguous_feature WHERE portrait_id = :portraitId")
    suspend fun getUnambiguousFeatures(portraitId: Int): List<UnambiguousFeature>

    @Query("SELECT * FROM similar_species WHERE portrait_id = :portraitId")
    suspend fun getSimilarSpecies(portraitId: Int): List<SimilarSpecies>

    @Query("SELECT * FROM good_to_know WHERE portrait_id = :portraitId")
    suspend fun getGoodToKnows(portraitId: Int): List<GoodToKnow>

}
