/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CurrentVersionDao {
    @Query("SELECT version FROM species_current_version LIMIT 1")
    suspend fun getCurrentVersion(): Int
}