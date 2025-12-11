/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.specieslist

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.backend.ViewPortraitOperation
import berlin.mfn.naturblick.databinding.FragmentSpeciesListBinding
import berlin.mfn.naturblick.ui.species.ConfirmSpecies
import berlin.mfn.naturblick.ui.species.PickSpecies.SELECTED_SPECIES
import berlin.mfn.naturblick.ui.species.PickSpecies.SPECIES_SELECTABLE
import berlin.mfn.naturblick.ui.species.PickSpeciesResult
import berlin.mfn.naturblick.ui.species.portrait.SpeciesId
import berlin.mfn.naturblick.utils.AndroidDeviceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class SpeciesListFragment : Fragment() {

    private val confirmSpeciesResult = registerForActivityResult(ConfirmSpecies) { confirmed ->
        if (confirmed != null) {
            finish(confirmed)
        }
    }

    private fun finish(confirmed: PickSpeciesResult) {
        val intent = Intent()
        intent.putExtra(SELECTED_SPECIES, confirmed)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    private var showSpeciesDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val selectable = requireActivity().intent.extras?.getBoolean(SPECIES_SELECTABLE) ?: false
        val navController = findNavController()
        val speciesListViewModel: SpeciesListViewModel by activityViewModels()
        val binding = FragmentSpeciesListBinding.inflate(inflater, container, false)
        val recyclerView = binding.rvSpeciesList
        val pagingAdapter = SpeciesListAdapter { species ->
            if (selectable) {
                confirmSpeciesResult.launch(SpeciesId(species.species.id))
            } else {
                speciesListViewModel.countViewPortrait(speciesId = species.species.id)
                navController.navigate(
                    SpeciesListFragmentDirections.actionNavPortraitsToNavPortrait(
                        SpeciesId(species.species.id),
                        false
                    )
                )
            }
        }

        recyclerView.adapter = pagingAdapter
        speciesListViewModel.pagingData.observe(viewLifecycleOwner) { pagingData ->
            pagingAdapter.submitData(lifecycle, pagingData)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.rvSpeciesList) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = insets.bottom)
            windowInsets
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        showSpeciesDialog?.dismiss()
    }
}
