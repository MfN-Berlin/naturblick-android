package berlin.mfn.naturblick.ui.info.account

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.databinding.FragmentSignUpBinding
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.setSingleClickListener
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignUpFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val model by viewModels<SignUpViewModel>()

        bindProgressButton(binding.signUp)

        binding.signUp.setSingleClickListener {
            binding.signUpError.visibility = View.GONE
            binding.signUp.isEnabled = false
            binding.signUp.showProgress {
                buttonTextRes = R.string.in_progress
                progressColor = resources.getColor(R.color.white)
            }
            lifecycleScope.launch {
                NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                    try {
                        PublicBackendApi.service.signUp(
                            requireContext(),
                            email = binding.email.text.toString(),
                            password = binding.password.text.toString()
                        )
                        Pair(true, null)
                    } catch (e: HttpException) {
                        if (e.code() == 409) {
                            Pair(false, R.string.user_already_exists)
                        } else {
                            throw e
                        }
                    }
                }.fold({ (success, message) ->
                    if (success) {
                        Settings.setEmailAndRequireSignIn(
                            requireContext(),
                            binding.email.text.toString()
                        )
                        showValidateEmailDialog {
                            findNavController().navigate(
                                SignUpFragmentDirections.actionNavSignUpToNavSignIn(false)
                            )
                        }
                    } else {
                        binding.signUpError.setText(message!!)
                        binding.signUpError.visibility = View.VISIBLE
                        binding.signUp.hideProgress(R.string.sign_up)
                        binding.signUp.isEnabled = true
                    }
                }, { error ->
                    Toast.makeText(
                        requireContext(),
                        error.error,
                        Toast.LENGTH_LONG
                    ).show()
                    binding.signUp.hideProgress(R.string.sign_up)
                    binding.signUp.isEnabled = true
                })
            }
        }
        model.setValidationStatusCallback { email, password, passwordMessage ->
            binding.emailLayout.error = if (email != null && !email) {
                requireContext().getString(R.string.email_is_not_valid)
            } else {
                null
            }
            binding.passwordLayout.error = if (password != null && !password) {
                requireContext().getString(passwordMessage!!)
            } else {
                null
            }
        }
        model.setIsValidCallback {
            binding.signUp.isEnabled = it
        }
        binding.model = model
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun showValidateEmailDialog(done: () -> Unit) {
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.Naturblick_MaterialComponents_Dialog_Alert
        ).apply {
            setTitle(R.string.validate_email_title)
            setMessage(R.string.validate_email_message)
            setPositiveButton(R.string.open_default_email_app) { _, _ ->
                openEmailApp(done)
            }
            setNegativeButton(R.string.go_to_login_screen) { _, _ ->
                done()
            }
            setOnCancelListener {
                done()
            }
        }.show()
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
            findNavController().navigate(
                SignUpFragmentDirections.actionNavSignUpToNavSignIn(false)
            )
        }

    private fun openEmailApp(done: () -> Unit) {
        try {
            startEmail.launch(Unit)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                R.string.default_email_app_failed,
                Toast.LENGTH_LONG
            ).show()
            done()
        }
    }
}
