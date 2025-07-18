/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.character

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentCharacterBinding
import berlin.mfn.naturblick.ui.species.specieslist.SpeciesListActivity
import berlin.mfn.naturblick.ui.species.specieslist.SpeciesListActivity.Companion.QUERY_CHARACTERS
import berlin.mfn.naturblick.ui.species.specieslist.SpeciesListActivity.Companion.QUERY_IS_OPEN
import berlin.mfn.naturblick.ui.species.specieslist.SpeciesListActivity.Companion.QUERY_TITLE
import berlin.mfn.naturblick.utils.setSingleClickListener
import berlin.mfn.naturblick.utils.setupBottomInsetMargin

class CharacterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args: CharacterFragmentArgs by navArgs()
        (requireActivity() as CharacterActivity).supportActionBar?.title = args.groupName
        val binding = FragmentCharacterBinding.inflate(inflater, container, false)
        val viewModel: CharacterViewModel by viewModels {
            CharacterViewModelFactory(
                args.group,
                requireActivity().application
            )
        }
        viewModel.state.observe(viewLifecycleOwner) {
            if(it.selected.isEmpty()) {
                binding.searchButton.visibility = View.GONE
            } else {
                binding.searchButton.visibility = View.VISIBLE
            }
            binding.characterList.setCharacterValues(it) { valueId ->
                viewModel.toggleValue(valueId)
            }
        }
        viewModel.numberOfResults.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.searchButton.text =
                    requireContext().getString(R.string.show_results, it.toString())
                binding.searchButton.setIconResource(R.drawable.outline_check_24)
                binding.searchButton.isEnabled = true
            } else {
                binding.searchButton.setText(R.string.show_no_results)
                binding.searchButton.setIconResource(R.drawable.outline_close_24)
                binding.searchButton.isEnabled = false
            }
        }
        binding.searchButton.setSingleClickListener {
            startActivity(
                Intent(context, SpeciesListActivity::class.java).apply {
                    putExtra(QUERY_CHARACTERS, viewModel.state.value?.query)
                    putExtra(QUERY_TITLE, args.groupName)
                    putExtra(QUERY_IS_OPEN, false)
                }
            )
        }
        binding.searchButton.setupBottomInsetMargin()
        binding.model = viewModel
        binding.searchButton.isExtended = true
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }
}
