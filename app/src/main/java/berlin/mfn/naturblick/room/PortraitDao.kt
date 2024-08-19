/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.*

@Dao
interface PortraitDao {
    @Query("SELECT * FROM portrait WHERE species_id = :speciesId AND language = :language")
    suspend fun getPortrait(speciesId: Int, language: Int): FullPortrait?

    @Query("SELECT * FROM portrait WHERE species_id = :speciesId AND language = :language")
    suspend fun getMinimalPortrait(speciesId: Int, language: Int): Portrait?
}
