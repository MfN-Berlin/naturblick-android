/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import android.os.Parcelable
import android.util.Log
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.info.settings.Settings
import com.bumptech.glide.load.engine.GlideException
import kotlinx.parcelize.Parcelize
import okio.IOException
import retrofit2.HttpException

@Parcelize
data class RecoverableError(val error: Int, val isSignedOut: Boolean = false) : Parcelable

data class NetworkResult<T : Any> internal constructor(
    val success: T?,
    val error: RecoverableError?,
    val isSignedOut: Boolean = false
) {
    fun <U> fold(s: (T) -> U, e: (RecoverableError) -> Unit): U? =
        if (success != null) {
            s(success)
        } else {
            e(error!!)
            null
        }

    suspend fun <R : Any> map(block: suspend (T) -> R): NetworkResult<R> =
        if (success != null) {
            NetworkResult(block(success), null)
        } else {
            NetworkResult(null, error, isSignedOut)
        }

    suspend fun <R : Any> flatMap(block: suspend (T) -> NetworkResult<R>): NetworkResult<R> =
        if (success != null) {
            block(success)
        } else {
            NetworkResult(null, error, isSignedOut)
        }

    companion object {
        const val TAG = "NetworkResult"
        suspend fun <T : Any> catchNetworkAndServerErrors(
            context: Context,
            block: suspend () -> T
        ): NetworkResult<T> {
            return try {
                NetworkResult(block(), null)
            } catch (e: HttpException) {
                Log.e(TAG, "Failed to interact with server. HttpException [ ${e.message} ]", e)
                if (e.code() == 401) {
                    Settings.setSignedOut(context)
                    NetworkResult(
                        null,
                        RecoverableError(
                            R.string.error_signed_out, true
                        )
                    )
                } else {
                    NetworkResult(
                        null,
                        RecoverableError(
                            R.string.error_response
                        )
                    )
                }
            } catch (e: IOException) {
                Log.e(TAG, "Failed to interact with server. IOException [ ${e.message} ]", e)
                NetworkResult(
                    null,
                    RecoverableError(
                        R.string.no_connection
                    )
                )
            } catch (g: GlideException) {
                Log.e(TAG, "Failed to interact with server. GlideException [ ${g.message} ]", g)
                g
                    .rootCauses
                    .filterIsInstance<com.bumptech.glide.load.HttpException>()
                    .firstOrNull()?.let { httpException ->
                        if (
                            httpException.statusCode !=
                            com.bumptech.glide.load.HttpException.UNKNOWN
                        ) {
                            if (httpException.statusCode == 401) {
                                Settings.setSignedOut(context)
                                NetworkResult(
                                    null,
                                    RecoverableError(R.string.error_signed_out, true)
                                )
                            } else {
                                NetworkResult(null, RecoverableError(R.string.error_response))
                            }
                        } else {
                            NetworkResult(null, RecoverableError(R.string.no_connection))
                        }
                    } ?: throw g
            }
        }

        fun <T : Any> success(t: T): NetworkResult<T> = NetworkResult(t, null)

        fun <T>ioToFileException(block: () -> T): T {
            try {
                return block()
            } catch (ioe: IOException) {
                throw FileException("IOException converted to FileException", ioe)
            }
        }
    }
}
