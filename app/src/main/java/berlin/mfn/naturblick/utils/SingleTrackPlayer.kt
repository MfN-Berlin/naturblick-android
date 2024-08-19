/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class SingleTrackPlayer(context: Context) {
    private val player = ExoPlayer.Builder(context).build()
    private var onIsPlayingChanged: ((Boolean) -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                this@SingleTrackPlayer.onIsPlayingChanged?.let {
                    it(isPlaying)
                }
            }
        })
    }

    fun setOnIsPlayingChangedListener(listener: (Boolean) -> Unit) {
        onIsPlayingChanged = listener
    }

    val currentPosition: Long get() =
        player.currentPosition

    val isPlaying: Boolean get() =
        player.isPlaying

    fun stop() {
        player.stop()
    }

    fun start(uri: Uri, start: Long? = null, end: Long? = null) {
        val clipping = if (start != null || end != null) {
            MediaItem.ClippingConfiguration.Builder().apply {
                if (start != null)
                    setStartPositionMs(start)
                if (end != null)
                    setEndPositionMs(end)
            }.build()
        } else {
            null
        }
        player.setMediaItem(
            MediaItem.Builder().apply {
                setUri(uri)
                if (clipping != null)
                    setClippingConfiguration(clipping)
            }.build()
        )
        player.prepare()
        player.playWhenReady = true
    }

    fun toggle(uri: Uri, start: Long? = null, end: Long? = null) {
        if (player.isPlaying) {
            stop()
        } else {
            start(uri, start, end)
        }
    }

    fun release() {
        player.release()
    }
}
