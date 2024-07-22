package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.PublicBackendApi
import berlin.mfn.naturblick.databinding.FragmentFieldbookBinding
import berlin.mfn.naturblick.ui.fieldbook.*
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.NetworkResult
import berlin.mfn.naturblick.utils.setSingleClickListener
import kotlinx.coroutines.launch

class FieldbookFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController()
        val binding = FragmentFieldbookBinding.inflate(inflater, container, false)
        val fieldbookViewModel: FieldbookViewModel by activityViewModels() {
            FieldbookViewModelFactory(requireActivity().application)
        }
        binding.refreshFieldbook.setOnRefreshListener {
            if (Settings.canSync(requireContext())) {
                lifecycleScope.launch {
                    NetworkResult.catchNetworkAndServerErrors(requireContext()) {
                        PublicBackendApi.service.triggerSync(requireContext())
                    }.fold(
                        {
                            binding.refreshFieldbook.isRefreshing = false
                        }, { error ->
                        makeText(
                            requireContext(),
                            error.error,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.refreshFieldbook.isRefreshing = false
                    }
                    )
                }
            } else {
                (requireActivity() as FieldbookActivity).onSignedOut()
                binding.refreshFieldbook.isRefreshing = false
            }
        }
        val recyclerView = binding.rvFieldbookList
        val pagingAdapter = FieldbookAdapter { observation ->
            navController.navigate(
                FieldbookFragmentDirections.navFieldBookListToNavFieldBookObservation(
                    OpenObservation(observation.occurenceId)
                )
            )
        }
        recyclerView.adapter = pagingAdapter
        pagingAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) { // New observations are added on top
                    // Scroll to new observation
                    recyclerView.scrollToPosition(positionStart)
                }
            }
        })
        fieldbookViewModel.pagingData.observe(viewLifecycleOwner) { pagingData ->
            pagingAdapter.submitData(lifecycle, pagingData)
        }
        binding.mapButton.setSingleClickListener { _ ->
            navController.navigate(
                FieldbookFragmentDirections.navFieldBookListToNavFieldBookMap()
            )
        }
        binding.createObservationAction.setSingleClickListener {
            (requireActivity() as FieldbookActivity).manageObservation.launch(
                CreateManualObservation()
            )
        }
        binding.createObservationPhotoAction.setSingleClickListener { _ ->
            (requireActivity() as FieldbookActivity).manageObservation.launch(
                CreateImageObservation
            )
        }
        binding.createObservationAudioAction.setSingleClickListener { _ ->
            (requireActivity() as FieldbookActivity).manageObservation.launch(
                CreateAudioObservation
            )
        }
        binding.createObservationGalleryAction.setSingleClickListener { _ ->
            if (Settings.hasSeenImportInfo(requireContext())) {
                (requireActivity() as FieldbookActivity).manageObservation.launch(
                    CreateImageFromGalleryObservation
                )
            } else {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(R.string.import_info_title)
                    setMessage(R.string.import_info)
                    setPositiveButton(R.string.continue_str) { _, _ ->
                        launchImportFromGallery()
                    }
                    setOnCancelListener {
                        launchImportFromGallery()
                    }
                }.show()
            }
        }
        return binding.root
    }

    private fun launchImportFromGallery() {
        Settings.didSeeImportInfo(requireContext())
        (requireActivity() as FieldbookActivity).manageObservation.launch(
            CreateImageFromGalleryObservation
        )
    }
}
