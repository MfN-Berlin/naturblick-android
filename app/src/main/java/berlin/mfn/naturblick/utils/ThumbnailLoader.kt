/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import berlin.mfn.naturblick.backend.*
import berlin.mfn.naturblick.utils.AndroidDeviceId.glideHeaders
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.*
import java.io.File
import java.io.InputStream

class ThumbnailLoader(
    private val glideUrlLoader: ModelLoader<GlideUrl, InputStream>,
    private val fileLoader: ModelLoader<File, InputStream>,
    private val resourceLoader: ModelLoader<Int, InputStream>,
    private val context: Context
) : ModelLoader<ThumbnailRequest, InputStream> {
    override fun buildLoadData(
        model: ThumbnailRequest,
        width: Int,
        height: Int,
        options: Options
    ): ModelLoader.LoadData<InputStream>? =
        when (val thumbnail = model.thumbnail(context)) {
            is AuthRemoteThumbnail -> {
                val url = GlideUrl(thumbnail.url, glideHeaders(context))
                glideUrlLoader.buildLoadData(url, width, height, options)
            }
            is FileThumbnail -> fileLoader.buildLoadData(thumbnail.file, width, height, options)
            is RemoteThumbnail -> {
                val url = GlideUrl(thumbnail.url)
                glideUrlLoader.buildLoadData(url, width, height, options)
            }
            is ResourceThumbnail ->
                resourceLoader.buildLoadData(thumbnail.resourceId, width, height, options)
        }

    override fun handles(model: ThumbnailRequest): Boolean = true

    class Factory(
        private val applicationContext: Context
    ) : ModelLoaderFactory<ThumbnailRequest, InputStream> {

        override fun build(
            multiFactory: MultiModelLoaderFactory
        ): ModelLoader<ThumbnailRequest, InputStream> {
            val glideUrlLoader = multiFactory.build(GlideUrl::class.java, InputStream::class.java)
            val fileLoader = multiFactory.build(File::class.java, InputStream::class.java)
            val resourceLoader = multiFactory.build(Int::class.java, InputStream::class.java)
            return ThumbnailLoader(
                glideUrlLoader,
                fileLoader,
                resourceLoader,
                applicationContext
            )
        }

        override fun teardown() {}
    }
}
