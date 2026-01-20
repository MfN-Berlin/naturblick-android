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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentIdResultBinding
import berlin.mfn.naturblick.ui.idresult.IdResultActivityContractBase.Companion.ID_RESULT
import berlin.mfn.naturblick.ui.species.ConfirmSpecies
import berlin.mfn.naturblick.ui.species.PickFaunaSpecies
import berlin.mfn.naturblick.ui.species.PickSpecies
import berlin.mfn.naturblick.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class IdResultFragment : Fragment() {
    private lateinit var model: IdResultViewModel
    private var errorDialog: AlertDialog? = null
    private val findSpeciesResult = registerForActivityResult(PickSpecies) { speciesResult ->
        if (speciesResult != null) {
            finish(speciesResult.speciesId)
        }
    }
    private val findFaunaSpeciesResult = registerForActivityResult(PickFaunaSpecies) { speciesResult ->
        if (speciesResult != null) {
            finish(speciesResult.speciesId)
        }
    }

    private val confirmSpeciesResult = registerForActivityResult(ConfirmSpecies) { confirmed ->
        if(confirmed != null) {
            finish(confirmed.speciesId)
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

    private fun selectSpecies() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.other_identification)
            .apply {
                if (model.isNew) {
                    setItems(if (model.isImage) R.array.new_photo_options else R.array.new_sound_options) { _, chosen ->
                        when (chosen) {
                            0 -> finish(null)
                            1 -> cancel()
                            2 -> if (model.isSound) findFaunaSpeciesResult.launch(Unit) else findSpeciesResult.launch(Unit)
                            3 -> discard()
                            4 -> {} // Closes dialog
                        }
                    }
                } else {
                    setItems(if (model.isImage) R.array.photo_options else R.array.sound_options) { _, chosen ->
                        when (chosen) {
                            0 -> cancel()
                            1 -> if (model.isSound) findFaunaSpeciesResult.launch(Unit) else findSpeciesResult.launch(Unit)
                            2 -> discard()
                            3 -> {} // Closes dialog
                        }
                    }
                }
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel by activityViewModels<IdResultViewModel>()
        model = viewModel

        val binding = FragmentIdResultBinding.inflate(inflater, container, false)
        binding.model = model

        binding.root.setupBottomInset()
        model.idResults.observe(viewLifecycleOwner) { result ->
            if (result.isNotEmpty()) {
                binding.idResultListLayout.setIdResultList(
                    result.map { (species, backendIdResult) ->
                        IdResultWithSpecies(
                            species,
                            backendIdResult.score
                        ) {
                            confirmSpeciesResult.launch(it)
                        }
                    })
                binding.speciesLink.setSingleClickListener {
                    selectSpecies()
                }
                binding.loading.visibility = View.GONE
                binding.result.visibility = View.VISIBLE
                binding.name.setText(R.string.none_of_the_options)
            } else {
                binding.noSpeciesFound.setText(if(model.isImage) R.string.no_plants_found else R.string.no_animals_found)
                binding.noSpeciesFoundDescription.setText(if(model.isImage) R.string.no_plants_found_description else R.string.no_animals_found_description)
                binding.selectAgainName.setText(R.string.crop_again)
                binding.selectAgain.setSingleClickListener {
                    cancel()
                }
                if (model.isNew) {
                    binding.saveAsUnknown.visibility = View.VISIBLE
                    binding.saveAsUnknown.setSingleClickListener {
                        finish(null)
                    }
                    binding.discardName.setText(R.string.exit_without_saving_observation)
                    binding.discard.setSingleClickListener {
                        discard()
                    }
                    binding.selectSpeciesName.setText(R.string.select_species)
                    binding.selectSpecies.setSingleClickListener {
                        if (model.isSound) findFaunaSpeciesResult.launch(Unit) else findSpeciesResult.launch(Unit)
                    }
                } else {
                    binding.discardName.setText(R.string.select_species)
                    binding.discard.setSingleClickListener {
                        if (model.isSound) findFaunaSpeciesResult.launch(Unit) else findSpeciesResult.launch(Unit)
                    }
                    binding.selectSpeciesName.setText(R.string.cancel)
                    binding.selectSpecies.setSingleClickListener {
                        discard()
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
                        1 -> if (model.isSound) findFaunaSpeciesResult.launch(Unit) else findSpeciesResult.launch(Unit)
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
        errorDialog?.dismiss()
    }
}
