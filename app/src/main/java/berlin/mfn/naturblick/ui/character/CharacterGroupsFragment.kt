package berlin.mfn.naturblick.ui.character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentGroupsBinding
import berlin.mfn.naturblick.ui.data.Group.Companion.characterGroups
import berlin.mfn.naturblick.utils.AnalyticsTracker

class CharacterGroupsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentGroupsBinding.inflate(inflater, container, false)
        (requireActivity() as CharacterActivity)
            .supportActionBar?.setTitle(R.string.select_characteristics)

        binding.groupsGrid.setGroups(characterGroups) {

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
        binding.groupIcon.setImageResource(R.drawable.ic_characteristics24)
        return binding.root
    }
}
