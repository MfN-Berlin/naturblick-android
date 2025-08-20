/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [
        Index(value = ["name"], name = "idx_groups_name")
    ]
)
data class Group(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    val nature: String?,
    val gername: String?,
    val engname: String?,
    val has_portraits: Boolean,
    val is_fieldbookfilter: Boolean,
    val has_characters: Boolean
)



