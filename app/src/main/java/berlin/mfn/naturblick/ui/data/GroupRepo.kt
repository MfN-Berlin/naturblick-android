/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.room.StrapiDb
import kotlin.math.roundToInt

object GroupRepo {

    private var groupMapIcons: Map<String, Int>? = null

    private val bitmaps: MutableMap<String, Bitmap> = HashMap()
    private var unknownBitmap: Bitmap? = null
    private var defaultBitmap: Bitmap? = null

    suspend fun getFieldbookFilterGroupIds(context: Context): List<String> {
        val db = StrapiDb.getDb(context)
        return db.groupDao().getFieldbookGroups().map {
            it.name
        }
    }

    suspend fun getFieldbookFilterGroups(context: Context): List<UiGroup> {
        val db = StrapiDb.getDb(context)
        return db.groupDao().getFieldbookGroups().map {
            UiGroup(
                it.name,
                it.gername!!,
                it.engname!!,
                getImageResource(context, "group_${it.name}")
            )
        }
    }

    suspend fun getPortraitGroups(context: Context): List<UiGroup> {
        val db = StrapiDb.getDb(context)

        return db.groupDao().getGroups().filter {
            it.hasPortraits
        }.map {
            UiGroup(
                it.name,
                it.gername!!,
                it.engname!!,
                getImageResource(context, "group_${it.name}")
            )
        }
    }

    suspend fun getCharacterGroups(context: Context): List<UiGroup> {
        val db = StrapiDb.getDb(context)

        return db.groupDao().getGroups().filter {
            it.hasCharacters
        }.map {
            UiGroup(
                it.name,
                it.gername!!,
                it.engname!!,
                getImageResource(context, "group_${it.name}")
            )
        }
    }

    suspend fun getMapIcon(context: Context, id: String?): Bitmap {

        if (groupMapIcons == null) {
            groupMapIcons = getGroupMapIcons(context)
        }

        return if (id != null) {
            groupMapIcons!![id].let {
                if (it != null) {
                    bitmaps.getOrPut(id) {
                        bitmapFromDrawableRes(
                            context,
                            it
                        )!!
                    }
                } else {
                    defaultBitmap.let {
                        if (it == null) {
                            val bitmap = bitmapFromDrawableRes(context, R.drawable.ic_nbobs)!!
                            defaultBitmap = bitmap
                            bitmap
                        } else {
                            it
                        }
                    }
                }
            }
        } else {
            unknownBitmap.let {
                if (it == null) {
                    val bitmap = bitmapFromDrawableRes(context, R.drawable.ic_undefined_spec)!!
                    unknownBitmap = bitmap
                    bitmap
                } else {
                    it
                }
            }
        }
    }

    suspend fun getGroupMapIcons(context: Context): Map<String, Int> {
        val db = StrapiDb.getDb(context)
        return db.groupDao().getGroups()
            .associate { it.name to getImageResource(context, "ic_${it.name}") }
            .filter { it.value != 0 }
    }

    private fun getImageResource(context: Context, resource: String): Int {
        return context.resources.getIdentifier(
            resource,
            "drawable",
            context.packageName
        )
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(context, AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(context: Context, sourceDrawable: Drawable?): Bitmap? {
        return sourceDrawable?.let { drawable ->
            val constantState = drawable.constantState ?: return null
            val mutableDrawable = constantState.newDrawable().mutate()
            val mapPinSize = context.resources.getDimension(R.dimen.map_pin).roundToInt()

            val bitmap: Bitmap = createBitmap(mapPinSize, mapPinSize)
            val canvas = Canvas(bitmap)
            mutableDrawable.setBounds(0, 0, canvas.width, canvas.height)
            mutableDrawable.draw(canvas)
            bitmap
        }
    }
}