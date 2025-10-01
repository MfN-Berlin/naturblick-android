/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import berlin.mfn.naturblick.room.ImageWithSizes
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

class ImageWithSizesLoader(
    private val glideUrlLoader: ModelLoader<GlideUrl, InputStream>
) : ModelLoader<ImageWithSizes, InputStream> {
    override fun buildLoadData(
        model: ImageWithSizes,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? {
        val sorted = model.sizes.sortedBy {
            it.width
        }
        val optimal = sorted.find { it.width > width }
        val chosen = optimal ?: model.sizes.last()
        val url = GlideUrl(chosen.fullUrl)
        return glideUrlLoader.buildLoadData(url, width, height, options)
    }

    override fun handles(model: ImageWithSizes): Boolean = true

    class Factory : ModelLoaderFactory<ImageWithSizes, InputStream> {

        override fun build(
            multiFactory: MultiModelLoaderFactory
        ): ModelLoader<ImageWithSizes, InputStream> {
            return ImageWithSizesLoader(
                multiFactory.build(GlideUrl::class.java, InputStream::class.java)
            )
        }

        override fun teardown() {}
    }
}
