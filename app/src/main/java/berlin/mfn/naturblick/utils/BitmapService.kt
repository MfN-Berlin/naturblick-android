package berlin.mfn.naturblick.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale
import berlin.mfn.naturblick.utils.Media.Companion.JPEG_QUALITY
import kotlin.math.ceil

object BitmapService {

    fun writeBm(contentResolver: ContentResolver, bm: Bitmap, uri: Uri) {
        contentResolver.openOutputStream(uri)?.use {
            bm.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)
        }
    }

    fun decodeUri(contentResolver: ContentResolver, uri: Uri): Bitmap {
        contentResolver.openInputStream(uri).use { inputStream ->
            return BitmapFactory.decodeStream(inputStream)
        }
    }

    fun Bitmap.scale(factor: Float, height: Int): Bitmap {
        val newWidth = ceil(factor * width).toInt()
        val newHeight = height
        return scale(newWidth, newHeight)
    }
}
