package berlin.mfn.naturblick.ui.sound

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentCropSpectrogramBinding
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContract
import berlin.mfn.naturblick.ui.idresult.IdentifySpecies
import berlin.mfn.naturblick.ui.idresult.IdentifySpeciesSound
import berlin.mfn.naturblick.ui.sound.CropAndIdentifySound.CROP_AND_IDENTIFY_SOUND_RESULT
import berlin.mfn.naturblick.ui.species.PickSpecies
import berlin.mfn.naturblick.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.*

class CropSoundFragment : Fragment(), RequestedPermissionCallback {
    private lateinit var model: CropSoundViewModel
    private lateinit var binding: FragmentCropSpectrogramBinding
    private var errorDialog: AlertDialog? = null

    private val requestRead = registerLocalMediaReadPermissionRequest(
        this
    )

    override fun permissionResult(granted: Boolean) {
        model.getSpectrogram(granted)
    }

    private val idResultLauncher: ActivityResultLauncher<IdentifySpecies> =
        registerForActivityResult(
            IdResultActivityContract
        ) { result ->
            if (result != null) {
                save(result.speciesId, result.thumbnail)
            }
        }

    private val findSpeciesResult = registerForActivityResult(PickSpecies) { speciesResult ->
        if (speciesResult != null) {
            save(speciesResult.speciesId)
        }
    }

    private fun save(
        speciesId: Int?,
        thumbnail: MediaThumbnail? = null
    ) {
        val intent = Intent().apply {
            putExtra(
                CROP_AND_IDENTIFY_SOUND_RESULT,
                CropAndIdentifySoundResult(
                    speciesId, model.media.value!!, thumbnail
                )
            )
        }
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCropSpectrogramBinding.inflate(inflater, container, false)
        val viewModel by activityViewModels<CropSoundViewModel> {
            CropSoundViewModelFactory(requireActivity().application)
        }
        model = viewModel
        model.data.observe(viewLifecycleOwner) { result ->
            result.fold({ (uri, media, spectrogram, isNew, initialStart, initialEnd) ->
                binding.cropSpectrogramView.setUp(
                    spectrogram, uri, {
                        binding.timeView.text = it
                    }, { isPlaying ->
                    if (isPlaying) {
                        binding.buttonTogglePlay.setImageResource(
                            R.drawable.ic_baseline_pause_circle_outline_24
                        )
                    } else {
                        binding.buttonTogglePlay.setImageResource(
                            R.drawable.ic_baseline_play_circle_outline_24
                        )
                    }
                }, initialStart, initialEnd
                )
                model.setShowOnLeaveDialog {
                    showOnLeaveDialog(binding.cropSpectrogramView, it)
                }
                binding.buttonDelete.setSingleClickListener {
                    showOnLeaveDialog(binding.cropSpectrogramView) {
                        cancel()
                    }
                }
                binding.buttonConfirm.setSingleClickListener {
                    binding.cropSpectrogramView.stopPlayer()
                    lifecycleScope.launch {
                        val (thumbnail, startSegm, endSegm) = binding.cropSpectrogramView.crop(
                            requireContext()
                        )!!

                        idResultLauncher.launch(
                            IdentifySpeciesSound(
                                thumbnail, isNew, media, startSegm, endSegm
                            )
                        )
                    }
                }
                binding.buttonTogglePlay.setOnClickListener {
                    binding.cropSpectrogramView.togglePlayer()
                }
                binding.loading.visibility = View.GONE
                binding.buttonConfirm.visibility = View.VISIBLE
                binding.buttonDelete.visibility = View.VISIBLE
            }, {
                showErrorDialog(it)
            })
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val result = model.data.value
        if (result == null) {
            model.media.value?.availableWithoutPermission(requireContext(), {
                model.getSpectrogram(it)
            }, {
                requestRead.checkPermission(requireContext(), this)
            })
        } else if (result.error != null) {
            showErrorDialog(result.error)
        }
    }

    override fun onPause() {
        super.onPause()
        errorDialog?.dismiss()
    }

    private fun showErrorDialog(error: RecoverableError) {
        val dialog = errorDialog
        if (!(dialog != null && dialog.isShowing)) {
            errorDialog = MaterialAlertDialogBuilder(
                requireContext(), R.style.Naturblick_MaterialComponents_Dialog_Alert
            ).apply {
                setTitle(error.error)
                setOnCancelListener {
                    cancel()
                }
                setItems(model.errorOptions()) { _, chosen ->
                    when (chosen) {
                        0 -> {
                            model.media.value!!.availableWithoutPermission(requireContext(), {
                                model.getSpectrogram(it)
                            }, {
                                requestRead.checkPermission(
                                    requireContext(), this@CropSoundFragment
                                )
                            })
                        }
                        1 -> findSpeciesResult.launch(Unit)
                        2 -> save(null)
                    }
                }
            }.show()
        }
    }

    private fun showOnLeaveDialog(
        view: SpectrogramCropperView,
        leave: (Boolean) -> Unit
    ) {
        val dialogBuild = MaterialAlertDialogBuilder(requireContext(), R.style.Naturblick_MaterialComponents_Dialog_Alert)
        dialogBuild.setTitle(
            R.string.save_observation
        ).setMessage(
            R.string.save_observation_message
        ).setPositiveButton(R.string.save) { _, _ ->
            lifecycleScope.launch {
                save(
                    null, view.crop(requireContext())?.first
                )
            }
        }.setNegativeButton(
            R.string.exit_without_saving_observation
        ) { _, _ ->
            leave(true)
        }
        dialogBuild.show()
    }

    companion object {
        const val TAG = "CropSpectrogramFragment"
    }
}
