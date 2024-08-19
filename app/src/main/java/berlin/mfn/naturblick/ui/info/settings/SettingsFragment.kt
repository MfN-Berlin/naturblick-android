/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import berlin.mfn.naturblick.databinding.DeviceIdentifierBinding
import berlin.mfn.naturblick.databinding.FragmentSettingsBinding
import berlin.mfn.naturblick.utils.AndroidDeviceId
import berlin.mfn.naturblick.utils.setSingleClickListener

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val model by viewModels<SettingsViewModel> {
            SettingsViewModelFactory(requireActivity())
        }
        binding.model = model

        binding.saveSettings.setSingleClickListener {
            model.save()
        }

        val deviceIdentifier = AndroidDeviceId.deviceId(requireContext().contentResolver)
        val linkedDevices = Settings.getLinkedDevices(requireContext(), deviceIdentifier)
        linkedDevices.forEach { name ->
            DeviceIdentifierBinding.inflate(inflater, binding.linkedDevices, true).apply {
                deviceName = name
            }
        }
        return binding.root
    }
}
