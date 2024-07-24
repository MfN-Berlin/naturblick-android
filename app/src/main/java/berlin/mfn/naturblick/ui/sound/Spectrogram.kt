package berlin.mfn.naturblick.ui.sound

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.NetworkResult.Companion.catchNetworkAndServerErrors
import berlin.mfn.naturblick.utils.RemoteMedia
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Spectrogram {
    suspend fun remote(context: Context, media: RemoteMedia): NetworkResult<Bitmap> {
        val spectrogram = File(context.filesDir, "spectrogram_${media.id}.jpg")
        return withContext(Dispatchers.IO) {
            catchNetworkAndServerErrors(context) {
                if (!spectrogram.exists()) {

                    val bytes = PublicBackendApi.service.specgram(
                        media.id,
                        context
                    )
                    NetworkResult.ioToFileException {
                        spectrogram.createNewFile()
                        spectrogram.outputStream().use {
                            it.write(bytes)
                        }
                    }
                }
                NetworkResult.ioToFileException {
                    spectrogram.inputStream().use {
                        BitmapFactory.decodeStream(it)
                    }
                }
            }
        }
    }
}
