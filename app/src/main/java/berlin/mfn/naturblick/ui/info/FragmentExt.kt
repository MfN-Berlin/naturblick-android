package berlin.mfn.naturblick.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.BuildConfig
import java.net.URLEncoder

fun Fragment.openFeedback() {
    val deviceName = URLEncoder.encode(Build.MODEL)
    val appVersion = URLEncoder.encode(BuildConfig.VERSION_NAME)
    val url = Uri.parse(
        "https://survey.naturkundemuseum-berlin.de/de/Feedback%20Naturblick" +
            "?device_name=$deviceName&version=$appVersion"
    )
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            url
        )
    )
}
