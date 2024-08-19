/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.account

import android.os.Bundle
import androidx.activity.viewModels
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.BaseActivity

class AccountActivity : BaseActivity(R.navigation.account_navigation) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeNavigationViews()
        val model by viewModels<AccountViewModel>()
        model.setCloseOnFinished(intent.extras?.getBoolean(CLOSE_ON_FINISHED, true) ?: true)
    }

    companion object {
        const val CLOSE_ON_FINISHED = "close_on_finished"
    }
}
