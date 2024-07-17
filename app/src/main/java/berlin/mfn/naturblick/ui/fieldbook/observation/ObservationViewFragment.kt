package berlin.mfn.naturblick.ui.fieldbook.observation

import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentObservationViewBinding
import berlin.mfn.naturblick.ui.species.portrait.SpeciesId
import berlin.mfn.naturblick.utils.*
import kotlinx.coroutines.launch

class ObservationViewFragment : Fragment(), RequestedPermissionCallback {
    private lateinit var viewModel: ObservationViewModel
    private lateinit var binding: FragmentObservationViewBinding
    private lateinit var player: SingleTrackPlayer

    private val requestRead = registerLocalMediaReadPermissionRequest(
        this
    )

    override fun permissionResult(granted: Boolean) {
        viewModel.readPermissionResult(granted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = SingleTrackPlayer(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: ObservationViewModel by activityViewModels()
        viewModel.setRequestReadPermission {
            requestRead.checkPermission(requireContext(), this)
        }
        val editAction =
            ObservationViewFragmentDirections
                .navFieldBookViewObservationToFieldBookEditObservation()

        if (viewModel.isCreateFlow && !viewModel.createFlowLaunched) {
            // At start of create flow we go directly to edit view
            findNavController().navigate(editAction)
        }

        player.setOnIsPlayingChangedListener { isPlaying ->
            if (isPlaying) {
                binding.include.buttonTogglePlay.setImageResource(
                    R.drawable.ic_baseline_pause_circle_outline_24
                )
            } else {
                binding.include.buttonTogglePlay.setImageResource(
                    R.drawable.ic_baseline_play_circle_outline_24
                )
            }
        }

        this.viewModel = viewModel
        binding = FragmentObservationViewBinding.inflate(inflater, container, false)
        binding.model = viewModel
        binding.include.model = viewModel

        binding.bottomSheet.setUpRootAndTopSheet(
            binding.root,
            binding.include.root
        )

        binding.editButton.setSingleClickListener {
            findNavController().navigate(editAction)
        }

        binding.speciesLink.setSingleClickListener {
            viewModel.currentObservation.value.speciesIdState?.let {
                findNavController().navigate(
                    ObservationViewFragmentDirections.navFieldBookViewObservationToPortrait(
                        SpeciesId(it),
                        false
                    )
                )
            }
        }

        binding.locationLink.observationProperty.setSingleClickListener {
            viewModel.currentObservation.value.coordinatesState?.let {
                findNavController().navigate(
                    ObservationViewFragmentDirections.navFieldBookViewObservationToFieldBookMap(
                        ParcelUuid(viewModel.occurenceId)
                    )
                )
            }
        }

        lifecycleScope.launch {
            viewModel.media.collect { (media, granted, startAudio, start, end) ->
                if (media != null) {
                    if (media.type == MediaType.JPG) {
                        binding.include.buttonTogglePlay.hide()
                        binding.include.buttonFullscreen.show()
                        binding.include.buttonFullscreen.setOnClickListener {
                            findNavController().navigate(
                                ObservationViewFragmentDirections
                                    .navFieldBookViewObservationToFullscreen(media)
                            )
                        }
                    } else if (media.type == MediaType.MP4) {
                        binding.include.buttonTogglePlay.show()
                        binding.include.buttonFullscreen.hide()

                        binding.include.buttonTogglePlay.setOnClickListener {
                            lifecycleScope.launch {
                                togglePlay(media, granted, start, end)
                            }
                        }
                        if (startAudio) {
                            togglePlay(media, granted, start, end)
                            viewModel.clearStartAudioOnPermissionResult()
                        }
                    }
                }
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private suspend fun togglePlay(
        media: Media,
        granted: Boolean?,
        start: Int?,
        end: Int?
    ) {
        if (player.isPlaying) {
            player.stop()
        } else {
            if (granted == null || !granted) {
                media.availableWithoutPermission(requireContext(), {
                    player.start(it, start = start?.toLong(), end = end?.toLong())
                }, {
                    if (granted == null) { // We didn't ask for permission
                        requestRead.checkPermission(requireContext(), this)
                        viewModel.startAudioOnPermissionResult()
                    } else { // We do not have permission, use server
                        lifecycleScope.launch {
                            media.fetchUri(false, requireContext()).fold({ uri ->
                                player.start(uri, start = start?.toLong(), end = end?.toLong())
                            }, { error ->
                                Toast.makeText(
                                    requireContext(),
                                    error.error,
                                    Toast.LENGTH_LONG
                                ).show()
                            })
                        }
                    }

                })
            } else {
                media.fetchUri(granted, requireContext()).fold({ uri ->
                    player.start(uri, start = start?.toLong(), end = end?.toLong())
                }, { error ->
                    Toast.makeText(
                        requireContext(),
                        error.error,
                        Toast.LENGTH_LONG
                    ).show()
                })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
