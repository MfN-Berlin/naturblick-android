package berlin.mfn.naturblick.ui.info.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.databinding.FragmentAboutBinding
import berlin.mfn.naturblick.utils.setSingleClickListener

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAboutBinding.inflate(inflater, container, false)
        binding.feedbackEmail.setSingleClickListener {
            startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:naturblick@mfn-berlin.de"))
            )
        }
        return binding.root
    }
}
