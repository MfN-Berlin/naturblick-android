package berlin.mfn.naturblick.ui.photo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class ImageIdActivity : BaseActivity(
    R.navigation.image_id_navigation
) {
    private lateinit var viewModel: ImageIdViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = IntentCompat.getParcelableExtra(
            intent,
            CropAndIdentifyPhoto.CROP_AND_IDENTIFY_REQUEST,
            CropAndIdentifyPhotoRequest::class.java
        )!!
        val imageIdModel by viewModels<ImageIdViewModel> {
            ImageIdViewModelFactory(
                request,
                application
            )
        }
        viewModel = imageIdModel
        initializeNavigationViews()
    }

    override fun onLeave(leave: (Boolean) -> Unit): Boolean {
        return viewModel.onLeave(leave)
    }
}
