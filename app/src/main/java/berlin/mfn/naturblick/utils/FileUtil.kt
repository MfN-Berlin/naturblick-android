/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import berlin.mfn.naturblick.BuildConfig
import java.io.File

object FileUtil {

    fun File.toFileProviderUri(context: Context): Uri = FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        this
    )
}
