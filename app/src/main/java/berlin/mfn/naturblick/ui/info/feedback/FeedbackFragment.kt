package berlin.mfn.naturblick.ui.info.feedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.databinding.FragmentFeedbackBinding
import berlin.mfn.naturblick.utils.setSingleClickListener

class FeedbackFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        binding.feedbackEmail.setSingleClickListener {
            startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:naturblick@mfn.berlin"))
            )
        }
        return binding.root
    }
}
