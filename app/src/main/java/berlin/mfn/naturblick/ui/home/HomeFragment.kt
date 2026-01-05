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
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.backend.SyncWorker
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val navController = findNavController()
        val vm: HomeViewModel by viewModels()
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

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
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SyncWorker.triggerBackgroundSync(requireContext())
    }
}
