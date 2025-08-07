/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.specieslist

import android.app.Activity
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
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.databinding.FragmentSpeciesListBinding
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.species.PickSpecies.SELECTED_SPECIES
import berlin.mfn.naturblick.ui.species.PickSpecies.SPECIES_SELECTABLE
import berlin.mfn.naturblick.ui.species.PickSpeciesResult
import berlin.mfn.naturblick.ui.species.portrait.SpeciesId
import berlin.mfn.naturblick.utils.showSpeciesInfo

class SpeciesListFragment : Fragment() {

    private var showSpeciesDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val selectable = requireActivity().intent.extras?.getBoolean(SPECIES_SELECTABLE)

        val navController = findNavController()
        val speciesDao = StrapiDb.getDb(requireContext()).speciesDao()
        val speciesListViewModel: SpeciesListViewModel by activityViewModels {
            SpeciesListViewModelFactory(
                speciesDao
            )
        }
        val binding = FragmentSpeciesListBinding.inflate(inflater, container, false)
        val recyclerView = binding.rvSpeciesList
        val pagingAdapter = SpeciesListAdapter { species ->
            if (selectable == true) {
                showSpeciesDialog = showSpeciesInfo(layoutInflater, species.species, {
                    navController.navigate(
                        SpeciesListFragmentDirections.actionNavPortraitsToNavPortrait(
                            SpeciesId(species.species.id),
                            false
                        )
                    )
                }, {
                    val intent = Intent()
                    intent.putExtra(SELECTED_SPECIES, PickSpeciesResult(species.species.id))
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()
                })
            } else {
                navController.navigate(
                    SpeciesListFragmentDirections.actionNavPortraitsToNavPortrait(
                        SpeciesId(species.species.id),
                        true
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
