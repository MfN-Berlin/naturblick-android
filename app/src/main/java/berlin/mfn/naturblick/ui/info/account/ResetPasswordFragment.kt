package berlin.mfn.naturblick.ui.info.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.databinding.FragmentResetPasswordBinding
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.setSingleClickListener
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        val model by viewModels<ResetPasswordViewModel>()
        val args by navArgs<ResetPasswordFragmentArgs>()
        bindProgressButton(binding.resetPassword)

        binding.resetPassword.setSingleClickListener {
            binding.resetPassword.isEnabled = false
            binding.resetPassword.showProgress {
                buttonTextRes = R.string.in_progress
                progressColor = resources.getColor(R.color.white)
            }
            lifecycleScope.launch {
                model.password?.let { email ->
                    NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                        PublicBackendApi.service.resetPassword(args.token, email)
                    }.fold({ _ ->
                        makeText(
                            requireContext(),
                            R.string.password_reset_successful,
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigate(
                            ResetPasswordFragmentDirections.actionNavResetPasswordToNavSignIn(false)
                        )
                    }, { error ->
                        makeText(
                            requireContext(),
                            error.error,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.resetPassword.hideProgress(R.string.reset_password)
                        binding.resetPassword.isEnabled = true
                    })
                }
            }
        }
        model.setValidationStatusCallback { password, passwordMessage ->
            binding.passwordLayout.error = if (password != null && !password) {
                requireContext().getString(passwordMessage!!)
            } else {
                null
            }
        }
        model.setIsValidCallback {
            binding.resetPassword.isEnabled = it
        }
        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}
