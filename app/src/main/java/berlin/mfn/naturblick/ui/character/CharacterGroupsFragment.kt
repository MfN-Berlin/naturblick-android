/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentGroupsBinding
import berlin.mfn.naturblick.ui.data.GroupRepo
import berlin.mfn.naturblick.utils.AnalyticsTracker
import kotlinx.coroutines.launch

class CharacterGroupsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGroupsBinding.inflate(inflater, container, false)
        (requireActivity() as CharacterActivity)
            .supportActionBar?.setTitle(R.string.select_characteristics)

        viewLifecycleOwner.lifecycleScope.launch {
            context?.let { ctx ->
                binding.groupsGrid.setGroups(GroupRepo.getCharacterGroups(ctx)) {

                    AnalyticsTracker.trackWhichSpeciesMkey(
                        requireActivity().application as NaturblickApplication,
                        it
                    )

                    findNavController().navigate(
                        CharacterGroupsFragmentDirections.actionNavCharacterGroupsToNavCharacters(
                            it.id,
                            it.name
                        )
                    )
                }
            }
        }

        binding.groupIcon.setImageResource(R.drawable.ic_characteristics24)
        return binding.root
    }
}
