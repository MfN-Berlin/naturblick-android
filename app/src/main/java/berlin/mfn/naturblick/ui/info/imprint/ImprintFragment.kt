package berlin.mfn.naturblick.ui.info.imprint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.databinding.FragmentImprintBinding
import berlin.mfn.naturblick.utils.setSingleClickListener

class ImprintFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImprintBinding.inflate(inflater, container, false)
        binding.toSources.setSingleClickListener {
            findNavController().navigate(
                ImprintFragmentDirections.actionNavImprintToNavSourcesImprint()
            )
        }
        return binding.root
    }
}
