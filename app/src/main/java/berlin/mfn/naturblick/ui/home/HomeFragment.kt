/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.databinding.FragmentHomeBinding
import berlin.mfn.naturblick.ui.fieldbook.CreateAudioObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateImageObservation
import berlin.mfn.naturblick.utils.setSingleClickListener

class HomeFragment : Fragment() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.speciesPortraits.button.setSingleClickListener {
            navController.navigate(
                HomeFragmentDirections.actionNavStartToNavGroups()
            )
        }
        binding.fieldBook.button.setSingleClickListener {
            navController.navigate(
                HomeFragmentDirections.actionNavStartToNavFieldBook()
            )
        }
        binding.photographAPlant.button.setSingleClickListener {
            navController.navigate(
                HomeFragmentDirections.actionNavStartToNavFieldbookObservation(
                    CreateImageObservation
                )
            )
        }

        binding.recordABird.button.setSingleClickListener {
            navController.navigate(
                HomeFragmentDirections.actionNavStartToNavFieldbookObservation(
                    CreateAudioObservation
                )
            )
        }

        binding.selectCharacteristics.button.setSingleClickListener {
            navController.navigate(
                HomeFragmentDirections.actionNavStartToNavCharacters()
            )
        }
        return binding.root
    }
}
