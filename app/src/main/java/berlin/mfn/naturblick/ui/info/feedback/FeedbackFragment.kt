/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.databinding.FragmentFeedbackBinding
import berlin.mfn.naturblick.utils.sendFeedback
import berlin.mfn.naturblick.utils.setSingleClickListener

class FeedbackFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        binding.feedbackEmail.setSingleClickListener {
            sendFeedback()
        }
        return binding.root
    }
}
