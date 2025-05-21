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
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContractBase.Companion.ID_RESULT
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

    private fun discard() {
        requireActivity().setResult(Activity.RESULT_FIRST_USER)
        requireActivity().finish()
    }

    private fun destructiveOptions() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.other_identification)
            .setItems(R.array.autoid_destructive_options) { _, chosen ->
                when (chosen) {
                    0 -> discard()
                }
            }
            .show()
    }

    private fun selectSpecies() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.other_identification)
            .setItems(model.selectSpeciesItems) { _, chosen ->
                when (chosen) {
                    0 -> cancel()
                    1 -> findSpeciesResult.launch(Unit)
                    2 -> if(model.isNew) {
                        finish(null)
                    } else {
                        discard()
                    }
                    3 -> discard()
                }
            }
            .show()
    }

    private fun stopWaiting() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.other_identification)
            .setItems(model.stopWaitingItems) { _, chosen ->
                when (chosen) {
                    0 -> findSpeciesResult.launch(Unit)
                    1 -> if(model.isNew) {
                        finish(null)
                    } else {
                        discard()
                    }
                    2 -> discard()
                }
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val identifySpecies =
            IntentCompat.getParcelableExtra(
                requireActivity().intent,
                IdResultActivityContractBase.Companion.ID_SPECIES,
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

        binding.speciesLink.setSingleClickListener {
            stopWaiting()
        }

        model.idResults.observe(viewLifecycleOwner) { result ->
            if (result.isNotEmpty()) {
                binding.speciesLink.setSingleClickListener {
                    selectSpecies()
                }
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
                binding.loading.visibility = View.GONE
                binding.result.visibility = View.VISIBLE
                binding.name.setText(R.string.none_of_the_options)
            } else {
                binding.speciesLink.setSingleClickListener {
                    destructiveOptions()
                }
                binding.imageCropAgain.setSingleClickListener {
                    cancel()
                }
                binding.selectSpecies.setSingleClickListener {
                    findSpeciesResult.launch(Unit)
                }
                if(model.isNew) {
                    binding.saveAsUnknown.visibility = View.VISIBLE
                    binding.saveAsUnknown.setSingleClickListener {
                        finish(null)
                    }
                }
                binding.loading.visibility = View.GONE
                binding.noResult.visibility = View.VISIBLE
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
                setCancelable(false)
                setItems(model.errorOptions) { _, chosen ->
                    when (chosen) {
                        0 -> model.identify()
                        1 -> findSpeciesResult.launch(Unit)
                        2 -> if(model.isNew) {
                            finish(null)
                        } else {
                            discard()
                        }
                        3 -> discard()
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
        }
    }

    override fun onPause() {
        super.onPause()
        showSpeciesDialog?.dismiss()
        errorDialog?.dismiss()
    }
}
