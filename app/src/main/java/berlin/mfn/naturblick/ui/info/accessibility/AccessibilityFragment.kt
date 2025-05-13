/*
 * Copyright © 2025 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.accessibility

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.databinding.FragmentAccessibilityBinding
import berlin.mfn.naturblick.utils.sendAccessibilityFeedback
import berlin.mfn.naturblick.utils.sendFeedback
import berlin.mfn.naturblick.utils.setSingleClickListener

class AccessibilityFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAccessibilityBinding.inflate(inflater, container, false)
        binding.feedbackEmail.setSingleClickListener {
            sendAccessibilityFeedback()
        }
        return binding.root
    }
}
