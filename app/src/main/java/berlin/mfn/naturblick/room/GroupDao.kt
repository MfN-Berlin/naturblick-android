/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface GroupDao {
    @Query("SELECT * FROM `groups`")
    suspend fun getGroups(): List<Group>

    @Query("SELECT * FROM `groups` WHERE is_fieldbookfilter = True AND name IN (:obsGroups)")
    suspend fun getFieldbookGroups(obsGroups: List<String>): List<Group>

    @Query("SELECT * FROM `groups` WHERE has_portraits = True")
    suspend fun getPortraitGroups(): List<Group>

    @Query("SELECT * FROM `groups` WHERE has_characters = True")
    suspend fun getCharacterGroups(): List<Group>
}
