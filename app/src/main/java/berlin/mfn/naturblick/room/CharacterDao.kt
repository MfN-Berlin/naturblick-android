/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CharacterDao {
    @Query("SELECT * FROM character WHERE `group` = :group")
    suspend fun getCharacters(group: String): List<CharacterWithValues>
}
