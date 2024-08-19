/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.strapi

import kotlinx.serialization.Serializable


@Serializable
data class ImageFormat(val width: Int, val height: Int, val url: String)

@Serializable
data class ImageFormats(
    val large: ImageFormat? = null,
    val medium: ImageFormat? = null,
    val small: ImageFormat? = null,
    val thumbnail: ImageFormat? = null
)

@Serializable
data class ImageFile(
    val width: Int,
    val height: Int,
    val url: String,
    val formats: ImageFormats?
)

