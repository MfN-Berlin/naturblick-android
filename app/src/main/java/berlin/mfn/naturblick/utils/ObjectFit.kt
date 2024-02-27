package berlin.mfn.naturblick.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.math.roundToInt

data class Rect(val x: Int, val y: Int, val width: Int, val height: Int)

class ObjectFit(
    private val focus: Float
) : BitmapTransformation() {
    companion object {
        private const val ID = "berlin.mfn.naturblick.utils.ObjectFit"
        val ID_BYTES = ID.toByteArray(Charset.forName("UTF-8"))
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID_BYTES)
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val focus = focus / 100f
        val rect = if (
            toTransform.width.toFloat() / toTransform.height.toFloat() >
            outWidth.toFloat() / outHeight.toFloat()
        ) {
            val width =
                ((outWidth.toFloat() / outHeight.toFloat()) * toTransform.height).roundToInt()
            val widthSpace = toTransform.width - width
            Rect(
                (widthSpace * focus).roundToInt(),
                0,
                width,
                toTransform.height
            )
        } else {
            val height =
                ((outHeight.toFloat() / outWidth.toFloat()) * toTransform.width).roundToInt()
            val heightSpace = toTransform.height - height
            Rect(
                0,
                (heightSpace * focus).roundToInt(),
                toTransform.width,
                height,
            )
        }
        return Bitmap.createBitmap(toTransform, rect.x, rect.y, rect.width, rect.height)
    }

    override fun equals(other: Any?): Boolean {
        return other is ObjectFit
    }

    override fun hashCode(): Int {
        return ID.hashCode()
    }
}
