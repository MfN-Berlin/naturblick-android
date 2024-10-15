/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.feedback

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentFeedbackBinding
import berlin.mfn.naturblick.utils.setSingleClickListener
import de.cketti.mailto.EmailIntentBuilder

class FeedbackFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        binding.feedbackEmail.setSingleClickListener {
            EmailIntentBuilder.from(requireActivity())
                .to("naturblick@mfn.berlin")
                .subject(requireContext().resources.getString(R.string.feedback_subject, BuildConfig.VERSION_NAME))
                .body(requireContext().resources.getString(R.string.feedback_body, Build.MODEL, Build.VERSION.RELEASE, BuildConfig.VERSION_NAME))
                .start()
        }
        return binding.root
    }
}
