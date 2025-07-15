/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentGroupsBinding
import berlin.mfn.naturblick.ui.data.Group.Companion.portraitGroups
import berlin.mfn.naturblick.utils.AnalyticsTracker

class GroupsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGroupsBinding.inflate(inflater, container, false)
        binding.groupsGrid.setGroups(portraitGroups) {

            AnalyticsTracker.trackWhichSpeciesGroup(
                requireActivity().application as NaturblickApplication,
                it
            )

            findNavController().navigate(
                GroupsFragmentDirections.actionNavGroupsToNavPortraits(
                    null,
                    it.id,
                    it.name,
                    false
                )
            )
        }
        binding.groupIcon.setImageResource(R.drawable.ic_artportraits24)
        return binding.root
    }
}
