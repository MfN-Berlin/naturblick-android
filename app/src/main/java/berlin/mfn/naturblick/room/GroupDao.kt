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
}