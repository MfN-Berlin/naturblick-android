/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.utils.isGerman
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt

enum class GroupType {
    FLORA, FAUNA
}

@Parcelize
data class Group(
    val id: String,
    val gername: String,
    val engname: String,
    val image: Int,
    val type: GroupType
) : Parcelable {
    val name
        get() = if (isGerman()) {
            gername
        } else {
            engname
        }

    companion object {
        val groupMapIcons: Map<String, Int> = mapOf(
            Pair("acarida", R.drawable.ic_spiders),
            Pair("actinopterygii", R.drawable.ic_fish),
            Pair("amphibian", R.drawable.ic_amphibian),
            Pair("amphipoda", R.drawable.ic_crustaceans),
            Pair("anaspidea", R.drawable.ic_gastropoda),
            Pair("arachnid", R.drawable.ic_spiders),
            Pair("araneae", R.drawable.ic_spiders),
            Pair("bird", R.drawable.ic_bird),
            Pair("blattodea", R.drawable.ic_insects),
            Pair("branchiobdellida", R.drawable.ic_ringworm),
            Pair("branchiopoda", R.drawable.ic_crustaceans),
            Pair("bug", R.drawable.ic_insects),
            Pair("butterfly", R.drawable.ic_insects),
            Pair("cephalaspidea", R.drawable.ic_gastropoda),
            Pair("chilopoda", R.drawable.ic_centipede),
            Pair("coleoptera", R.drawable.ic_insects),
            Pair("conifer", R.drawable.ic_tree),
            Pair("crustacea", R.drawable.ic_crustaceans),
            Pair("dermaptera", R.drawable.ic_insects),
            Pair("diplopoda", R.drawable.ic_centipede),
            Pair("diptera", R.drawable.ic_insects),
            Pair("dragonfly", R.drawable.ic_insects),
            Pair("ephemeroptera", R.drawable.ic_insects),
            Pair("gastropoda", R.drawable.ic_gastropoda),
            Pair("grasshopper", R.drawable.ic_insects),
            Pair("herb", R.drawable.ic_herb),
            Pair("heteroptera", R.drawable.ic_insects),
            Pair("hirudinea", R.drawable.ic_ringworm),
            Pair("hydrachnidia", R.drawable.ic_spiders),
            Pair("hymenoptera", R.drawable.ic_insects),
            Pair("lepidoptera", R.drawable.ic_insects),
            Pair("mammal", R.drawable.ic_mammal),
            Pair("mantodea", R.drawable.ic_insects),
            Pair("maxillopoda", R.drawable.ic_crustaceans),
            Pair("mecoptera", R.drawable.ic_insects),
            Pair("megaloptera", R.drawable.ic_insects),
            Pair("neuroptera", R.drawable.ic_insects),
            Pair("odonata", R.drawable.ic_insects),
            Pair("oligochaeta", R.drawable.ic_ringworm),
            Pair("planipennia", R.drawable.ic_insects),
            Pair("plecoptera", R.drawable.ic_insects),
            Pair("polychaeta", R.drawable.ic_ringworm),
            Pair("psocoptera", R.drawable.ic_insects),
            Pair("raphidioptera", R.drawable.ic_insects),
            Pair("reptile", R.drawable.ic_reptile),
            Pair("strigeida", R.drawable.ic_bird),
            Pair("terebellida", R.drawable.ic_ringworm),
            Pair("thysanoptera", R.drawable.ic_insects),
            Pair("tree", R.drawable.ic_tree),
            Pair("trichoptera", R.drawable.ic_insects),
            Pair("truebug", R.drawable.ic_insects),
            Pair("zygentoma", R.drawable.ic_insects)
        )

        val groups = listOf(
            Group(
                "amphibian",
                "Amphibien",
                "Amphibians",
                R.drawable.group_amphibian,
                GroupType.FAUNA
            ),
            Group(
                "hymenoptera",
                "Bienen, Wespen & Co",
                "Bees, wasps & co",
                R.drawable.group_hymenoptera,
                GroupType.FAUNA
            ),
            Group(
                "conifer",
                "Nadelbäume",
                "Evergreens",
                R.drawable.group_conifer,
                GroupType.FLORA
            ),
            Group(
                "herb",
                "Kräuter & Wildblumen",
                "Herbs & Wild Flowers",
                R.drawable.group_herb,
                GroupType.FLORA
            ),
            Group(
                "tree",
                "Laubbäume & Ginkgo",
                "Deciduous trees & ginkgo",
                R.drawable.group_tree,
                GroupType.FLORA
            ),
            Group(
                "reptile",
                "Reptilien",
                "Reptiles",
                R.drawable.group_reptile,
                GroupType.FAUNA
            ),
            Group(
                "butterfly",
                "Schmetterlinge",
                "Butterflies",
                R.drawable.group_butterfly,
                GroupType.FAUNA
            ),
            Group(
                "gastropoda",
                "Schnecken",
                "Slugs",
                R.drawable.group_snail,
                GroupType.FAUNA
            ),
            Group(
                "mammal",
                "Säugetiere",
                "Mammals",
                R.drawable.group_mammal,
                GroupType.FAUNA
            ),
            Group(
                "bird",
                "Vögel",
                "Birds",
                R.drawable.group_bird,
                GroupType.FAUNA
            ),
            Group(
                "arachnid",
                "Spinnentiere",
                "Arachnid",
                0,
                GroupType.FAUNA
            ),
            Group(
                "truebug",
                "Schnabelkerfen",
                "True bug",
                0,
                GroupType.FAUNA
            ),
            Group(
                "heteroptera",
                "Wanzen",
                "Heteroptera",
                0,
                GroupType.FAUNA
            ),
            Group(
                "dragonfly",
                "Libellen",
                "Dragonfly",
                0,
                GroupType.FAUNA
            ),
            Group(
                "grasshopper",
                "Heuschrecken",
                "Grasshopper",
                0,
                GroupType.FAUNA
            ),
            Group(
                "diptera",
                "Zweiflügler",
                "Diptera",
                0,
                GroupType.FAUNA
            ),
            Group(
                "bug",
                "Käfer",
                "Bug",
                0,
                GroupType.FAUNA
            ),
            Group(
                "fungi",
                "Pilze",
                "Fungi",
                0,
                GroupType.FAUNA
            )
        )

        private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
            convertDrawableToBitmap(context, AppCompatResources.getDrawable(context, resourceId))

        private fun convertDrawableToBitmap(context: Context, sourceDrawable: Drawable?): Bitmap? {
            return sourceDrawable?.let { drawable ->
                val constantState = drawable.constantState ?: return null
                val mutableDrawable = constantState.newDrawable().mutate()
                val mapPinSize = context.resources.getDimension(R.dimen.map_pin).roundToInt()

                val bitmap: Bitmap = Bitmap.createBitmap(
                    mapPinSize, mapPinSize,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                mutableDrawable.setBounds(0, 0, canvas.width, canvas.height)
                mutableDrawable.draw(canvas)
                bitmap
            }
        }

        private val bitmaps: MutableMap<String, Bitmap> = HashMap()
        private var unknownBitmap: Bitmap? = null
        private var defaultBitmap: Bitmap? = null

        fun getMapIcon(context: Context, id: String?): Bitmap = if (id != null) {
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

        private val characterGroupIds = listOf(
            "amphibian",
            "hymenoptera",
            "herb",
            "tree",
            "reptile",
            "butterfly",
            "mammal",
            "bird",
            "gastropoda"
        )

        private val portraitGroupIds = listOf(
            "amphibian",
            "hymenoptera",
            "conifer",
            "herb",
            "tree",
            "reptile",
            "butterfly",
            "gastropoda",
            "mammal",
            "bird"
        )

        val fieldbookFilterGroupIds = listOf(
            "arachnid",
            "truebug",
            "heteroptera",
            "dragonfly",
            "grasshopper",
            "diptera",
            "gastropoda",
            "bug",
            "fungi",
            "amphibian",
            "reptile",
            "hymenoptera",
            "conifer",
            "mammal",
            "butterfly",
            "tree",
            "herb",
            "bird"
        )

        val characterGroups = groups.filter {
            characterGroupIds.contains(it.id)
        }

        val portraitGroups = groups.filter {
            portraitGroupIds.contains(it.id)
        }
    }
}
