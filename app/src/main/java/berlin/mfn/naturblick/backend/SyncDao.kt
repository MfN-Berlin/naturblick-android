/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SyncDao {
    @Transaction
    suspend fun newSync(syncId: Long?) {
        deleteAll()
        syncId?.let {
            insert(Sync(it))
        }
    }

    @Query("SELECT * FROM sync")
    suspend fun get(): Sync?

    @Insert
    suspend fun insert(sync: Sync)

    @Query("DELETE FROM sync")
    suspend fun deleteAll()
}
