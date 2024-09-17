/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.locationpicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.Coordinates
import berlin.mfn.naturblick.databinding.FragmentLocationpickerBinding
import berlin.mfn.naturblick.utils.MapFragment
import berlin.mfn.naturblick.utils.RequestedPermissionCallback
import berlin.mfn.naturblick.utils.registerRequestedOneOfPermission
import berlin.mfn.naturblick.utils.setSingleClickListener
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location

class LocationPickerFragment : MapFragment(), RequestedPermissionCallback {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLocationpickerBinding.inflate(inflater, container, false)

        binding.model = trackingModel
        val args by navArgs<LocationPickerFragmentArgs>()
        val mapView = binding.mapView
        initializeMapView(mapView, args.initialLocation) { map ->
            binding.toggleGps.setOnClickListener {
                requestLocationPermission.checkPermission(requireContext(), this)
            }
            val dropPinView = dropPin()
            mapView.addView(dropPinView)

            binding.selectLocationButton.setSingleClickListener {
                val position = map.coordinateForPixel(
                    ScreenCoordinate(
                        (dropPinView.left + (dropPinView.width / 2)).toDouble(),
                        dropPinView.bottom.toDouble()
                    )
                )
                val intent = Intent()
                intent.putExtra(
                    PICKED_LOCATION,
                    Coordinates(position.latitude(), position.longitude())
                )
                requireActivity().setResult(Activity.RESULT_OK, intent)
                requireActivity().finish()
            }
            mapView.location.updateSettings {
                locationPuck = LocationPuck2D() // Hides location puck
            }
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun dropPin(): ImageView {
        val dropPinView = ImageView(requireContext())
        dropPinView.setImageResource(R.drawable.ic_baseline_edit_location_24)
        dropPinView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.secondary_200))
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        val density = resources.displayMetrics.density
        params.bottomMargin = (12 * density).toInt()
        dropPinView.layoutParams = params
        return dropPinView
    }

    override fun permissionResult(granted: Boolean) {
        trackingModel.startTracking()
    }

    private val requestLocationPermission =
        registerRequestedOneOfPermission(
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            this
        )

    companion object {
        const val TAG = "LocationPickerFragment"
        const val PICKED_LOCATION = "picked_location"
        const val INITIAL_LOCATION = "initial_location"
    }
}
