/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.databinding.FragmentSignInBinding
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.setSingleClickListener
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignInFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)
        val model by activityViewModels<AccountViewModel>()
        val existingEmail = Settings.getEmail(requireContext())
        if (existingEmail != null)
            binding.email.setText(existingEmail)

        binding.forgotPassword.setSingleClickListener {
            findNavController().navigate(
                SignInFragmentDirections.actionNavSignInToNavForgotPassword()
            )
        }

        binding.signIn.setSingleClickListener {
            binding.signInError.visibility = View.GONE
            binding.signIn.isEnabled = false
            binding.signIn.showProgress {
                buttonTextRes = R.string.in_progress
                progressColor = ContextCompat.getColor(requireContext(), R.color.white)
            }
            lifecycleScope.launch {
                NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                    try {
                        PublicBackendApi.service.signIn(
                            requireContext(),
                            binding.email.text.toString(),
                            binding.password.text.toString()
                        )
                        true
                    } catch (e: HttpException) {
                        if (e.code() == 400) {
                            false
                        } else {
                            throw e
                        }
                    }
                }.fold({ success ->
                    if (success) {
                        makeText(
                            requireContext(),
                            requireContext().getString(
                                R.string.signed_in_as,
                                binding.email.text.toString()
                            ),
                            LENGTH_LONG
                        ).show()
                        if (model.closeOnFinished) {
                            findNavController().navigate(
                                SignInFragmentDirections.actionNavSignInToNavStart()
                            )
                        } else {
                            findNavController().navigateUp()
                        }
                    } else {
                        binding.signInError.visibility = View.VISIBLE
                        binding.signIn.hideProgress(R.string.sign_in)
                        binding.signIn.isEnabled = true
                    }
                }, { error ->
                    makeText(
                        requireContext(),
                        error.error,
                        LENGTH_LONG
                    ).show()
                    binding.signIn.hideProgress(R.string.sign_in)
                    binding.signIn.isEnabled = true
                })
            }
        }
        return binding.root
    }
}
