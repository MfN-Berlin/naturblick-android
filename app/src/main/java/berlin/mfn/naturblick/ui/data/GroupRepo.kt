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
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.room.StrapiDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap

object GroupRepo {

    private lateinit var fieldbookFilterGroupIds: List<String>
    private lateinit var groupMapIcons: Map<String, Int>
    private lateinit var portraitGroups: List<UiGroup>
    private lateinit var characterGroups: List<UiGroup>
    private lateinit var fieldbookFilterGroups: List<UiGroup>

    private val bitmaps: MutableMap<String, Bitmap> = HashMap()
    private var unknownBitmap: Bitmap? = null
    private var defaultBitmap: Bitmap? = null

    fun getFieldbookFilterGroupIds(): List<String> {
        return fieldbookFilterGroupIds
    }

    fun getFieldbookFilterGroups(): List<UiGroup> {
        return fieldbookFilterGroups
    }

    fun getPortraitGroups(): List<UiGroup> {
        return portraitGroups
    }

    fun getCharacterGroups(): List<UiGroup> {
        return characterGroups
    }

    fun getMapIcon(context: Context, id: String?): Bitmap =
        if (id != null) {
            groupMapIcons.get(id).let {
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

    fun init(context: Context) {
        val db = StrapiDb.getDb(context)
        CoroutineScope(Dispatchers.IO).launch {
            val allGroups = db.groupDao().getGroups()
            fieldbookFilterGroupIds = allGroups.filter {
                it.isFieldbookfilter
            }.map {
                it.name
            }

            portraitGroups = allGroups.filter {
                it.hasPortraits && it.nature != null
            }.map {
                UiGroup(
                    it.name,
                    it.gername!!,
                    it.engname!!,
                    getImageResource(context, "group_${it.name}"),
                    if (it.nature == "fauna") GroupType.FAUNA else GroupType.FLORA
                )
            }

            characterGroups = allGroups.filter {
                it.hasCharacters && it.nature != null
            }.map {
                UiGroup(
                    it.name,
                    it.gername!!,
                    it.engname!!,
                    getImageResource(context, "group_${it.name}"),
                    if (it.nature == "fauna") GroupType.FAUNA else GroupType.FLORA
                )
            }

            fieldbookFilterGroups = allGroups.filter {
                it.isFieldbookfilter && it.nature != null
            }.map {
                UiGroup(
                    it.name,
                    it.gername!!,
                    it.engname!!,
                    getImageResource(context, "group_${it.name}"),
                    if (it.nature == "fauna") GroupType.FAUNA else GroupType.FLORA
                )
            }

            groupMapIcons =
                allGroups.associate { it.name to getImageResource(context, "ic_${it.name}") }
                    .filter { it.value != 0 }
        }
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