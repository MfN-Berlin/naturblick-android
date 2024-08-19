/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openDatabase
import android.provider.MediaStore
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class OldMedia(val src: String? = null)

@Serializable
data class OldObservation(val media: OldMedia? = null)

object OldDB {
    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    private fun localId(context: Context, filename: String): Long? {
        val likeFilename = "%$filename%"
        val selectionNewFiles =
            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionOldFiles =
            "${MediaStore.MediaColumns.DATA} LIKE ?"
        return context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.MediaColumns._ID),
            "$selectionNewFiles OR $selectionOldFiles",
            arrayOf(likeFilename, likeFilename),
            null
        )?.use { cursor ->
            if (cursor.count > 0) {
                cursor.moveToFirst()
                cursor.getLong(0)
            } else {
                null
            }
        }
    }

    fun findLocalMediaId(context: Context, obsIdent: String): String? {
        val dbFile = File(context.filesDir, "userobs.db").toString()
        try {
            return openDatabase(dbFile, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                db.rawQuery(
                    """
                    |SELECT json FROM "by-sequence" 
                    |WHERE doc_id = "$obsIdent" 
                    |ORDER BY CAST(substr(rev, 0, instr(rev, '-')) AS INTEGER) DESC LIMIT 1;
                    |""".trimMargin(),
                    null
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(0)
                    } else {
                        null
                    }
                }?.let { json ->
                    jsonDecoder.decodeFromString<OldObservation>(json).media?.src
                }?.let { src ->
                    val q = src.split("/").lastOrNull()?.split(".")?.firstOrNull()
                    q?.let { filename ->
                        Pair(src, filename)
                    }
                }?.let { (src, filename) ->
                    localId(context, filename)?.let { id ->
                        "$id;$src"
                    }
                }
            }
        } catch (_: SQLException) {
            return null
        }
    }
}
