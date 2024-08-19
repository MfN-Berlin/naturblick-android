/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.databinding.FragmentAccountBinding
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.setSingleClickListener
import com.github.razir.progressbutton.bindProgressButton
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = requireActivity().intent?.data

        if (data != null && data.pathSegments.size == 3 && data.pathSegments[1] == "activate") {
            val activationToken = data.pathSegments.last()
            lifecycleScope.launch {
                NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                    PublicBackendApi.service.activateAccount(activationToken)
                }.fold({
                    findNavController().navigate(
                        AccountFragmentDirections.actionNavAccountToNavSignIn(true)
                    )
                }, { error ->
                    makeText(
                        requireContext(),
                        error.error,
                        LENGTH_LONG
                    ).show()
                })
            }
        } else if (data != null && data.pathSegments.size == 2 &&
            data.pathSegments[1].startsWith("reset-password")
        ) {
            val token = data.getQueryParameter("token")
            if (token != null) {
                findNavController().navigate(
                    AccountFragmentDirections.actionNavAccountToNavResetPassword(token)
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAccountBinding.inflate(inflater, container, false)
        bindProgressButton(binding.deleteAccount)

        val model by activityViewModels<AccountViewModel>()
        binding.signIn.setSingleClickListener {
            findNavController().navigate(
                AccountFragmentDirections.actionNavAccountToNavSignIn(false)
            )
        }
        binding.signInAfterRegister.setSingleClickListener {
            findNavController().navigate(
                AccountFragmentDirections.actionNavAccountToNavSignIn(false)
            )
        }
        binding.signUp.setSingleClickListener {
            findNavController().navigate(
                AccountFragmentDirections.actionNavAccountToNavSignUp()
            )
        }
        binding.signUpAgain.setSingleClickListener {
            Settings.clearEmail(requireContext())
            findNavController().navigate(
                AccountFragmentDirections.actionNavAccountToNavSignUp()
            )
        }
        binding.signInAgain.setSingleClickListener {
            findNavController().navigate(
                AccountFragmentDirections.actionNavAccountToNavSignIn(false)
            )
        }
        binding.signOut.setSingleClickListener {
            Settings.clearEmail(requireContext())
        }
        binding.signOutAfterRegister.setSingleClickListener {
            Settings.clearEmail(requireContext())
        }
        binding.deleteAccount.setSingleClickListener {
            findNavController().navigate(
                AccountFragmentDirections.actionNavAccountToNavDeleteAccount()
            )
        }
        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}
