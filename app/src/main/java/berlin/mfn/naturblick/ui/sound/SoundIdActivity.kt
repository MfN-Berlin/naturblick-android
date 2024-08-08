package berlin.mfn.naturblick.ui.sound

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class SoundIdActivity : BaseActivity(
    R.navigation.sound_id_navigation
) {
    private lateinit var model: CropSoundViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val request = IntentCompat.getParcelableExtra(
            intent,
            CropAndIdentifySound
                .MEDIA,
            CropAndIdentifySoundRequest::class.java
        )
        val viewModel: CropSoundViewModel by viewModels {
            CropSoundViewModelFactory(application)
        }
        model = viewModel
        if (request != null) {
            model.setRequest(request, false)
            initializeNavigationViews(
                alternativeStart = R.id.nav_crop_sound
            )
        } else {
            initializeNavigationViews()
        }
    }

    override fun onLeave(leave: (Boolean) -> Unit): Boolean {
        model.onLeave(leave)
        return false
    }
}
