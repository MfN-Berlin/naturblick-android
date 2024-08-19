/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.ContentResolver
import android.content.Context
import android.provider.Settings
import berlin.mfn.naturblick.ui.info.settings.Settings.getAllDeviceIds
import berlin.mfn.naturblick.ui.info.settings.Settings.getToken
import com.bumptech.glide.load.model.Headers
import com.bumptech.glide.load.model.LazyHeaders
import java.math.BigInteger
import java.security.MessageDigest

data class AuthHeaders(val deviceId: String?, val token: String?)

object AndroidDeviceId {
    private var deviceId: String? = null
    private var headers: Headers? = null
    private var authHeaders: AuthHeaders? = null

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray()))
            .toString(16)
            .padStart(32, '0')
    }

    fun deviceId(contentResolver: ContentResolver): String {
        val deviceId = AndroidDeviceId.deviceId
        if (deviceId == null) {
            val d = md5(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
            AndroidDeviceId.deviceId = d
            return d
        } else {
            return deviceId
        }
    }

    private fun deviceIds(context: Context): Set<String> {
        val deviceId = deviceId(context.contentResolver)
        return getAllDeviceIds(context, deviceId)
    }

    private fun deviceIdHeader(context: Context): String =
        deviceIds(context).joinToString(separator = ",")

    fun glideHeaders(context: Context): Headers {
        val localHeaders = headers
        return if (localHeaders == null) {
            val newHeaders = LazyHeaders
                .Builder().apply {
                    val (deviceIdHeader, bearerToken) = authHeaders(context)
                    if (bearerToken != null)
                        addHeader("Authorization", bearerToken)
                    else
                        addHeader("X-MfN-Device-Id", deviceIdHeader!!)
                }
                .build()
            headers = newHeaders
            newHeaders
        } else {
            localHeaders
        }
    }

    fun invalidateToken() {
        authHeaders = null
        headers = null
    }

    fun authHeaders(context: Context): AuthHeaders {
        val local = authHeaders
        val current = if (local != null) {
            local
        } else {
            val token = getToken(context)
            if (token != null) {
                AuthHeaders(null, "Bearer $token")
            } else {
                AuthHeaders(deviceIdHeader(context), null)
            }
        }
        authHeaders = current
        return current
    }
}
