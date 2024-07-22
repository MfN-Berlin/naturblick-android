package berlin.mfn.naturblick.ui.sound

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.scale
import androidx.core.view.ViewCompat
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.utils.LocalMediaThumbnail
import berlin.mfn.naturblick.utils.Media
import berlin.mfn.naturblick.utils.MediaPlayerService.timeFormat
import berlin.mfn.naturblick.utils.MediaThumbnail
import berlin.mfn.naturblick.utils.SingleTrackPlayer
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpectrogramCropperView(context: Context, attrs: AttributeSet?) :
    AppCompatImageView(context, attrs), Runnable {

    data class State(
        val uri: Uri,
        val bitmap: Bitmap,
        val timeTextListener: (String) -> Unit,
        val isPlayingListener: (Boolean) -> Unit
    )

    // Movement state
    // This state keep tracks of the delta x of the movement and the type of movement.
    // Either the user is moving the selected area (Moving) or resizing it
    // from the start or end (Resizing(start = true/false))
    // Start captures the initial position of the movement to calculate the first delta x.
    sealed interface Movement
    object None : Movement
    data class Start(val x: Float) : Movement
    data class Moving(val x: Float, val dx: Float) : Movement
    data class Resizing(val x: Float, val dx: Float, val start: Boolean) : Movement

    private var start: Float = 0f
        set(value) {
            field = value
            invalidate()
            stopPlayer()
        }
    private var end: Float = 1f
        set(value) {
            field = value
            invalidate()
            stopPlayer()
        }

    private fun startMs(spectrogram: Bitmap): Int =
        (start * spectrogram.width * PIX_TO_MS_FACTOR).roundToInt()

    private fun endMs(spectrogram: Bitmap): Int =
        (end * spectrogram.width * PIX_TO_MS_FACTOR).roundToInt()

    private val startPx get() = start * width
    private val endPx get() = end * width
    private var move: Movement = None
    private val strokeWidth: Float get() = width * 0.01f
    private val touchSnap: Float get() = strokeWidth * 5f
    private fun touches(touch: Float, x: Float): Boolean =
        abs(x - touch) < touchSnap

    private var state: State? = null
    private var player: SingleTrackPlayer? = null

    private fun intervalSizeBiggerThanMin(spectrogram: Bitmap, start: Float, end: Float): Boolean =
        (end * spectrogram.width - start * spectrogram.width) > MIN_PIX

    fun setUp(
        bm: Bitmap,
        uri: Uri,
        timeTextListener: (String) -> Unit,
        isPlayingListener: (Boolean) -> Unit,
        initialStart: Int?,
        initialEnd: Int?
    ) {
        if (bm.width > SAFE_BITMAP_WIDTH) {
            super.setImageBitmap(bm.scale(SAFE_BITMAP_WIDTH, bm.height))
        } else {
            super.setImageBitmap(bm)
        }
        start = if (initialStart != null) {
            maxOf(minOf(initialStart / (bm.width.toFloat() * PIX_TO_MS_FACTOR), 1f), 0f)
        } else {
            maxOf(0, bm.width - PIX_TO_SEE).toFloat() / bm.width.toFloat()
        }
        end = if (initialEnd != null) {
            maxOf(minOf(initialEnd / (bm.width.toFloat() * PIX_TO_MS_FACTOR), 1f), 0f)
        } else {
            1f
        }
        player = SingleTrackPlayer(context).apply {
            setOnIsPlayingChangedListener { isPlaying ->
                updateText()
                state?.isPlayingListener?.let {
                    it(isPlaying)
                }
            }
        }
        state = State(uri, bm, timeTextListener, isPlayingListener)
    }

    private fun updateText() {
        player?.let {
            state?.let { (_, spectrogram, timeTextListener, _) ->
                val text = if (it.isPlaying) {
                    timeFormat(it.currentPosition / 1000f)
                } else {
                    timeFormat(((endMs(spectrogram) - startMs(spectrogram)) / 1000f))
                }
                timeTextListener(text)
            }
        }
    }

    override fun run() {
        updateText()
        handler?.postDelayed(this, 100)
    }

    fun stopPlayer() {
        player?.stop()
    }

    fun togglePlayer() {
        player?.let {
            state?.let { (uri, spectrogram, _, _) ->
                it.toggle(uri, startMs(spectrogram).toLong(), endMs(spectrogram).toLong())
                updateText()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { motion ->
            state?.let { (_, spectrogram, _, _) ->
                move = when (val current = move) {
                    is None ->
                        if (event.action == ACTION_DOWN) {
                            Start(event.x)
                        } else {
                            None
                        }
                    is Start ->
                        if (event.action == ACTION_MOVE) {
                            if (touches(motion.x, startPx)) {
                                Resizing(event.x, event.x - current.x, true)
                            } else if (touches(motion.x, endPx)) {
                                Resizing(event.x, event.x - current.x, false)
                            } else {
                                Moving(event.x, event.x - current.x)
                            }
                        } else {
                            None
                        }
                    is Moving ->
                        if (event.action == ACTION_MOVE) {
                            Moving(event.x, event.x - current.x)
                        } else {
                            None
                        }
                    is Resizing ->
                        if (event.action == ACTION_MOVE) {
                            Resizing(event.x, event.x - current.x, current.start)
                        } else {
                            None
                        }
                }
                when (val new = move) {
                    is Moving -> {
                        val newStart = (startPx + new.dx) / width
                        val newEnd = (endPx + new.dx) / width
                        if (newStart >= 0f && newEnd <= 1f) {
                            start = newStart
                            end = newEnd
                        }
                    }
                    is Resizing -> if (new.start) {
                        val newStart = (startPx + new.dx) / width
                        if (newStart >= 0) {
                            start = if (
                                intervalSizeBiggerThanMin(spectrogram, newStart, end)
                            ) {
                                newStart
                            } else {
                                maxOf(0f, end - MIN_PIX / spectrogram.width.toFloat())
                            }
                        }
                    } else {
                        val newEnd = (endPx + new.dx) / width
                        if (newEnd <= 1f) {
                            end = if (
                                intervalSizeBiggerThanMin(spectrogram, start, newEnd)
                            ) {
                                newEnd
                            } else {
                                minOf(1f, start + MIN_PIX / spectrogram.width.toFloat())
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
        return true
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val edgeWidth = resources.getDimension(R.dimen.android_gesture_inset).toInt()
        val edgeOffset = (resources.getDimension(R.dimen.android_gesture_disable_max) / 2).toInt()
        val edgeCenter = height / 2
        // Disable system gestures over spectrogram
        ViewCompat.setSystemGestureExclusionRects(
            this,
            listOf(
                Rect(
                    width - edgeWidth,
                    edgeCenter - edgeOffset,
                    width,
                    edgeCenter + edgeOffset
                ),
                Rect(
                    0,
                    edgeCenter - edgeOffset,
                    edgeWidth,
                    edgeCenter + edgeOffset
                )
            )
        )
        canvas?.apply {
            val rect = RectF(
                startPx + strokeWidth / 2f,
                strokeWidth / 2f,
                endPx - strokeWidth / 2f,
                height.toFloat() - strokeWidth / 2f
            )
            val handleTop = height / 2f + height / 5f
            val handleBottom = height / 2f - height / 5f
            val startHandleX = startPx + touchSnap / 2f
            val endHandleX = endPx - touchSnap / 2f
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = resources.getColor(R.color.white_opacity_60)
                style = Paint.Style.STROKE
                strokeWidth = width * 0.01f
            }
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = resources.getColor(R.color.white_opacity_10)
                style = Paint.Style.FILL
            }
            val grayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = resources.getColor(R.color.black_opacity_10)
                style = Paint.Style.FILL
            }
            drawRect(0f, 0f, startPx, height.toFloat(), grayPaint)
            drawRect(endPx, 0f, width.toFloat(), height.toFloat(), grayPaint)
            drawRect(rect, fillPaint)
            drawRect(rect, strokePaint)
            drawLine(startHandleX, handleTop, startHandleX, handleBottom, strokePaint)
            drawLine(endHandleX, handleTop, endHandleX, handleBottom, strokePaint)
        }
    }

    suspend fun crop(context: Context): Triple<LocalMediaThumbnail, Int, Int>? =
        state?.let { (_, spectrogram, _, _) ->
            withContext(Dispatchers.IO) {
                val thumbnail = MediaThumbnail.createEmpty(context)
                try {
                    thumbnail.file.outputStream().use { os ->
                        val cropSize = resources.getDimension(R.dimen.crop_size).roundToInt()
                        Bitmap
                            .createBitmap(
                                spectrogram,
                                (start * spectrogram.width).roundToInt(),
                                0,
                                (end * spectrogram.width - start * spectrogram.width).roundToInt(),
                                spectrogram.height
                            )
                            .scale(cropSize, cropSize)
                            .compress(Bitmap.CompressFormat.JPEG, Media.JPEG_QUALITY, os)
                    }
                    Triple(
                        thumbnail.externallySuccessfullyCreated(),
                        startMs(spectrogram),
                        endMs(spectrogram)
                    )
                } catch (e: Exception) {
                    thumbnail.delete()
                    throw e
                }
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        player?.release()
    }

    companion object {
        // 100 Pixel = 1 sec
        const val PIX_TO_MS_FACTOR = 10
        const val PIX_TO_SEE = 400
        const val MIN_PIX = 400
        const val SAFE_BITMAP_WIDTH = 2048
    }
}
