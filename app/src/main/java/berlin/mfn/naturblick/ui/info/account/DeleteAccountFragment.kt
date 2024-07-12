package berlin.mfn.naturblick.ui.info.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.databinding.FragmentDeleteAccountBinding
import berlin.mfn.naturblick.utils.AndroidDeviceId
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.setSingleClickListener
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DeleteAccountFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDeleteAccountBinding.inflate(inflater, container, false)

        binding.forgotPassword.setSingleClickListener {
            findNavController().navigate(
                SignInFragmentDirections.actionNavSignInToNavForgotPassword()
            )
        }

        // Token may be null if background tasks hits a 401
        val (_, token) = AndroidDeviceId.authHeaders(requireContext())
        if (token != null) {
            lifecycleScope.launch {
                NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                    PublicBackendApi.service.deleteCount(requireContext(), token)
                }.fold({ count ->
                    binding.loading.visibility = View.GONE
                    if (count.numDetachedObservations != 0) {
                        binding.deleteAccountQuestion.text = getString(
                            R.string.delete_account_question_with_observations,
                            count.numDetachedObservations
                        )
                    } else {
                        // If 0 observations will be unlinked, there is no warning
                        binding.deleteAccountQuestion.visibility = View.GONE
                    }
                    binding.deleteAccountLoaded.visibility = View.VISIBLE
                }, { _ ->
                    binding.loading.visibility = View.GONE
                    binding.deleteAccountLoaded.visibility = View.VISIBLE
                })
            }
        } else {
            binding.loading.visibility = View.GONE
            binding.deleteAccountLoaded.visibility = View.VISIBLE
        }

        binding.deleteAccount.setSingleClickListener {
            deleteLoading(binding)
            binding.deleteAccountError.visibility = View.GONE
            lifecycleScope.launch {
                NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                    try {
                        PublicBackendApi.service.delete(
                            requireContext(),
                            binding.email.text.toString(),
                            binding.password.text.toString()
                        )
                        true
                    } catch (e: HttpException) {
                        if (e.code() == 400) {
                            binding.deleteAccountError.visibility = View.VISIBLE
                            deleteStopLoading(binding)
                            false
                        } else {
                            throw e
                        }
                    }
                }.fold({ deleted ->
                    if (deleted) {
                        makeText(
                            requireContext(),
                            R.string.account_delete,
                            LENGTH_LONG
                        ).show()
                        findNavController().navigateUp()
                    }
                    deleteStopLoading(binding)
                }, { error ->
                    val errorMsg =
                        "${getText(R.string.delete_account_failed)} ${getText(error.error)}"
                    makeText(
                        requireContext(),
                        errorMsg,
                        LENGTH_LONG
                    ).show()
                    deleteStopLoading(binding)
                })
            }
        }

        binding.forgotPassword.setSingleClickListener {
            findNavController().navigate(
                DeleteAccountFragmentDirections.actionNavDeleteAccountToNavForgotPassword(true)
            )
        }
        return binding.root
    }

    private fun deleteLoading(binding: FragmentDeleteAccountBinding) {
        binding.deleteAccount.isEnabled = false
        binding.deleteAccount.showProgress {
            buttonTextRes = R.string.in_progress
            progressColor = ContextCompat.getColor(requireContext(), R.color.white)
        }
    }

    private fun deleteStopLoading(binding: FragmentDeleteAccountBinding) {
        binding.deleteAccount.hideProgress(R.string.delete_account)
        binding.deleteAccount.isEnabled = true
    }
}
