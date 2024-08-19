/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import berlin.mfn.naturblick.backend.ThumbnailRequest
import berlin.mfn.naturblick.room.ImageWithSizes
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSizeBytes = 1024 * 1024 * 1024 // 1 GB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(
            ThumbnailRequest::class.java, InputStream::class.java,
            ThumbnailLoader.Factory(context)
        )
        registry.append(
            ImageWithSizes::class.java, InputStream::class.java,
            ImageWithSizesLoader.Factory()
        )
    }
}
