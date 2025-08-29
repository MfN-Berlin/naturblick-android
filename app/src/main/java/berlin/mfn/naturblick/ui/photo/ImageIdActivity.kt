/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.photo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.IntentCompat
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class ImageIdActivity : BaseActivity(
    R.navigation.image_id_navigation
) {
    private lateinit var viewModel: ImageIdViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageIdModel by viewModels<ImageIdViewModel> {
            ImageIdViewModel.Factory
        }
        viewModel = imageIdModel
        initializeNavigationViews()
    }

    override fun onLeave(leave: (Boolean) -> Unit): Boolean {
        return viewModel.onLeave(leave)
    }
}
