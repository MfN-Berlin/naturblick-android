package berlin.mfn.naturblick.ui.fieldbook.fullscreen

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import berlin.mfn.naturblick.databinding.FragmentFullscreenBinding
import berlin.mfn.naturblick.utils.RemoteMedia
import berlin.mfn.naturblick.utils.RequestedPermissionCallback
import berlin.mfn.naturblick.utils.registerLocalMediaReadPermissionRequest
import kotlinx.coroutines.launch

class FullscreenFragment : Fragment(), RequestedPermissionCallback {
    private lateinit var media: RemoteMedia
    private lateinit var binding: FragmentFullscreenBinding
    private val requestRead = registerLocalMediaReadPermissionRequest(
        this
    )

    override fun permissionResult(granted: Boolean) {
        lifecycleScope.launch {
            media.fetchUri(granted, requireContext()).fold(
                { uri ->
                    binding.speciesImage.setImageURI(uri)
                }, { error ->
                requireActivity().setResult(Activity.RESULT_CANCELED)
                requireActivity().finish()
                Toast.makeText(
                    requireContext(),
                    error.error,
                    Toast.LENGTH_LONG
                ).show()
            }
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        media = requireActivity().intent.extras?.getParcelable(
            MEDIA
        )!!

        binding = FragmentFullscreenBinding.inflate(inflater, container, false)
        media.availableWithoutPermission(
            requireContext(),
            {
                binding.speciesImage.setImageURI(it)
            },
            {
                requestRead.checkPermission(requireContext(), this)
            }
        )
        return binding.root
    }

    companion object {
        const val MEDIA = "media"
    }
}
