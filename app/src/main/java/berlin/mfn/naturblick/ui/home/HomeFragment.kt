/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.backend.ViewFieldbookOperation
import berlin.mfn.naturblick.databinding.FragmentHomeBinding
import berlin.mfn.naturblick.ui.fieldbook.CreateAudioObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateImageObservation
import berlin.mfn.naturblick.utils.AndroidDeviceId
import berlin.mfn.naturblick.utils.setSingleClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    val operationDao = ObservationDb.getDb(application).operationDao()

    fun countViewFieldbook() {
        viewModelScope.launch(Dispatchers.IO) {
            val deviceIdentifier = AndroidDeviceId.deviceId(application.contentResolver)
            operationDao.insertOperation(
                ViewFieldbookOperation(
                    deviceIdentifier,
                    ZonedDateTime.now()
                )
            )
        }
    }
}

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: HomeViewModel by viewModels()

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.speciesPortraits.button.setSingleClickListener {
            navController.navigate(
                HomeFragmentDirections.actionNavStartToNavGroups()
            )
        }
        binding.fieldBook.button.setSingleClickListener {
            vm.countViewFieldbook()
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
    }
}
