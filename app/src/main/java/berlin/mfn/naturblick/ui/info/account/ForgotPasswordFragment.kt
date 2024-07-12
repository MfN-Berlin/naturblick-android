package berlin.mfn.naturblick.ui.info.account

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.databinding.FragmentForgotPasswordBinding
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.setSingleClickListener
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        val model by viewModels<ForgotPasswordViewModel>()
        val args by navArgs<ForgotPasswordFragmentArgs>()
        bindProgressButton(binding.resetPassword)

        binding.resetPassword.setSingleClickListener {
            binding.resetPassword.isEnabled = false
            binding.resetPassword.showProgress {
                buttonTextRes = R.string.in_progress
                progressColor = ContextCompat.getColor(requireContext(), R.color.white)
            }
            lifecycleScope.launch {
                model.email?.let { email ->
                    NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                        PublicBackendApi.service.forgotPassword(email)
                    }.fold({ _ ->
                        showDialog(args.inDeleteFlow) {
                            findNavController().navigateUp()
                        }
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
        model.setValidationStatusCallback { email ->
            binding.emailLayout.error = if (email != null && !email) {
                requireContext().getString(R.string.email_is_not_valid)
            } else {
                null
            }
        }
        model.setIsValidCallback {
            binding.resetPassword.isEnabled = it
        }
        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner
        model.email = Settings.getEmail(requireContext())
        return binding.root
    }

    private val startEmail =
        registerForActivityResult(object : ActivityResultContract<Unit, Unit>() {
            override fun createIntent(context: Context, input: Unit) =
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_EMAIL)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            override fun parseResult(resultCode: Int, intent: Intent?) {
            }
        }) {
            findNavController().navigateUp()
        }

    private fun showDialog(inDeleteFlow: Boolean, done: () -> Unit) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.Naturblick_MaterialComponents_Dialog_Alert
        ).apply {
            setTitle(R.string.reset_email_sent_title)
            setMessage(R.string.reset_email_sent_message)
            setPositiveButton(R.string.open_default_email_app) { _, _ ->
                openEmailApp(done)
            }
            val negativeButtonText = if (inDeleteFlow)
                R.string.go_back_to_delete_account
            else
                R.string.go_back_to_login_screen
            setNegativeButton(
                negativeButtonText
            ) { _, _ ->
                done()
            }
            setOnCancelListener {
                done()
            }
        }.show()
    }

    private fun openEmailApp(done: () -> Unit) {
        try {
            startEmail.launch(Unit)
        } catch (_: ActivityNotFoundException) {
            makeText(
                requireContext(),
                R.string.default_email_app_failed,
                Toast.LENGTH_LONG
            ).show()
            done()
        }
    }
}
