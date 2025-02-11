/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.idresult

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.IntentCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentIdResultBinding
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContract.ID_RESULT
import berlin.mfn.naturblick.ui.species.PickSpecies
import berlin.mfn.naturblick.ui.species.portrait.SpeciesId
import berlin.mfn.naturblick.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IdResultFragment : Fragment() {
    private lateinit var model: IdResultViewModel
    private var showSpeciesDialog: AlertDialog? = null
    private var errorDialog: AlertDialog? = null
    private val findSpeciesResult = registerForActivityResult(PickSpecies) { speciesResult ->
        if (speciesResult != null) {
            finish(speciesResult.speciesId)
        }
    }

    private fun finish(species: Int?) {
        requireActivity().setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(ID_RESULT, IdentifySpeciesResult(species, model.thumbnail))
            }
        )
        requireActivity().finish()
    }

    private fun selectSpecies() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.other_identification)
            .setItems(model.selectSpeciesItems) { _, chosen ->
                when (chosen) {
                    0 -> cancel()
                    1 -> findSpeciesResult.launch(Unit)
                    2 -> {
                        finish(null)
                    }
                }
            }
            .show()
    }
    private fun noSpeciesFound() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.no_species_found)
            setPositiveButton(if(model.isImage) R.string.crop_again else R.string.crop_sound_again) { _, _ ->
                cancel()
            }
            setNegativeButton(R.string.select_species) { _, _ ->
                findSpeciesResult.launch(Unit)
            }
            if(model.isNew) {
                setNeutralButton(R.string.save_without_species) { _, _ ->
                    finish(null)
                }
            }
            setMessage(R.string.no_species_found_description)
            setOnCancelListener {
                cancel()
            }
        }.show()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val identifySpecies =
            IntentCompat.getParcelableExtra(
                requireActivity().intent,
                IdResultActivityContract.ID_SPECIES,
                IdentifySpecies::class.java
            )!!
        val viewModel by activityViewModels<IdResultViewModel> {
            IdResultViewModelFactory(
                identifySpecies,
                requireActivity().application
            )
        }
        model = viewModel

        val binding = FragmentIdResultBinding.inflate(inflater, container, false)
        binding.model = model

        model.idResults.observe(viewLifecycleOwner) { result ->
            if (result.isNotEmpty()) {
                binding.idResultListLayout.setIdResultList(
                    result.map { (species, backendIdResult) ->
                        IdResultWithSpecies(
                            species,
                            backendIdResult.score
                        ) {
                            showSpeciesDialog = showSpeciesInfo(
                                inflater,
                                species,
                                {
                                    findNavController().navigate(
                                        IdResultFragmentDirections.actionNavIdResultToNavPortrait(
                                            SpeciesId(species.id),
                                            false
                                        )
                                    )
                                }, {
                                    finish(species.id)
                                }
                            )
                        }
                    })
                binding.speciesLink.setSingleClickListener {
                    selectSpecies()
                }
                binding.loading.visibility = View.GONE
                binding.result.visibility = View.VISIBLE
            } else {
                noSpeciesFound()
            }
        }

        model.recoverableError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showErrorDialog(error)
            }
        }

        return binding.root
    }

    private fun showErrorDialog(error: RecoverableError) {
        val dialog = errorDialog
        if (!(dialog != null && dialog.isShowing)) {
            errorDialog = MaterialAlertDialogBuilder(
                requireContext(),
                R.style.Naturblick_MaterialComponents_Dialog_Alert
            ).apply {
                setTitle(error.error)
                setOnCancelListener {
                    cancel()
                }
                setItems(model.errorOptions) { _, chosen ->
                    when (chosen) {
                        0 -> model.identify()
                        1 -> findSpeciesResult.launch(Unit)
                        2 -> finish(null)
                    }
                }
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        val error = model.recoverableError.value
        val result = model.idResults.value
        if (result == null && error != null) {
            showErrorDialog(error)
        } else if(result != null && result.isEmpty()) {
            noSpeciesFound()
        }
    }

    override fun onPause() {
        super.onPause()
        showSpeciesDialog?.dismiss()
        errorDialog?.dismiss()
    }
}
