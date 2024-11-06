/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.IntentCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentFieldbookMapBinding
import berlin.mfn.naturblick.databinding.ViewObservationPopupBinding
import berlin.mfn.naturblick.ui.data.Group.Companion.getMapIcon
import berlin.mfn.naturblick.ui.fieldbook.ManageObservation.OBSERVATION_ACTION
import berlin.mfn.naturblick.ui.fieldbook.OpenObservation
import berlin.mfn.naturblick.ui.fieldbook.fieldbook.FieldbookActivity.Companion.OCCURENCE_ID
import berlin.mfn.naturblick.ui.fieldbook.observation.ObservationActivity
import berlin.mfn.naturblick.utils.*
import com.google.gson.JsonPrimitive
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.viewport
import com.mapbox.maps.toCameraOptions
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import kotlinx.coroutines.flow.collectLatest
import java.util.*
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

class FieldbookMapFragment : Fragment() {
    private lateinit var binding: FragmentFieldbookMapBinding
    private lateinit var viewModel: FieldbookViewModel
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private val onMoveListener: OnMoveListener = object : OnMoveListener {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(detector: MoveGestureDetector) {
            viewModel.stopTracking()
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: FieldbookViewModel by activityViewModels {
            FieldbookViewModelFactory(requireActivity().application)
        }
        viewModel = model
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val initialOccurenceId = IntentCompat.getParcelableExtra(
            requireActivity().intent,
            OCCURENCE_ID,
            ParcelUuid::class.java
        )?.uuid
        binding = FragmentFieldbookMapBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        val map = mapView.getMapboxMap()
        val popupOffset = resources.getDimension(R.dimen.popup_offset).roundToInt()
        pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
        pointAnnotationManager.addClickListener { t ->
            t.getData()?.asString?.let { it ->
                val occurenceId = UUID.fromString(it)
                viewModel.selectObservation(occurenceId)
            }
            true
        }
        map.loadStyleUri(
            BuildConfig.STYLE_URL
        ) {
            map.gesturesPlugin { addOnMoveListener(onMoveListener) }
            viewModel.setStartTrackingListener {
                mapView.location.enabled = true
                mapView.viewport.transitionTo(
                    targetState = mapView.viewport.makeFollowPuckViewportState(
                        FollowPuckViewportStateOptions.Builder().apply {
                            pitch(null)
                        }.build()
                    ),
                    transition = mapView.viewport.makeImmediateViewportTransition()
                )
            }
            viewModel.setStopTrackingListener {
                mapView.location.enabled = false
                mapView.viewport.idle()
            }
            lifecycleScope.launch {
                viewModel.observationsFlow.flowWithLifecycle(lifecycle)
                    .collectLatest { observations ->
                        pointAnnotationManager.deleteAll()
                        val annotations: List<PointAnnotationOptions> =
                            observations.mapNotNull { observation ->
                                observation.coords?.let { coords ->
                                    val group = observation.species?.group
                                    getMapIcon(requireContext(), group).let { bitmap ->
                                        PointAnnotationOptions()
                                            .withPoint(Point.fromLngLat(coords.lon, coords.lat))
                                            .withIconImage(bitmap)
                                            .withIconAnchor(IconAnchor.BOTTOM)
                                            .withData(
                                                JsonPrimitive(observation.occurenceId.toString())
                                            )
                                    }
                                }
                            }
                        pointAnnotationManager.create(annotations)
                    }
            }
            viewModel.setObservationSelectedListener { pair ->
                mapView.viewAnnotationManager.removeAllViewAnnotations()
                pair?.let { (observation, moveTo) ->
                    observation.coords?.toPoint()?.let { location ->
                        if (moveTo) {
                            viewModel.stopTracking()
                            mapView.getMapboxMap().setCamera(
                                CameraOptions.Builder().apply {
                                    center(location)
                                    zoom(DEEP_ZOOM)
                                }.build()
                            )
                        }
                        popup(
                            location,
                            observation,
                            popupOffset
                        )
                    }
                }
            }
            viewModel.cameraState?.let {
                mapView.getMapboxMap().setCamera(it.toCameraOptions())
            } ?: initialOccurenceId?.let {
                viewModel.selectObservation(it, true)
            }
            map.addOnMapClickListener {
                viewModel.selectObservation(null)
                false
            }
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun popup(
        location: Geometry,
        observation: FieldbookObservation,
        offset: Int
    ) {
        val binding =
            ViewObservationPopupBinding.inflate(
                layoutInflater,
                mapView,
                false
            )
        binding.observation = observation
        binding.root.clipToOutline = true
        binding.button.setSingleClickListener {
            startActivity(
                Intent(requireContext(), ObservationActivity::class.java).putExtra(
                    OBSERVATION_ACTION,
                    OpenObservation(observation.occurenceId)
                )
            )
        }

        mapView.viewAnnotationManager.addViewAnnotation(
            binding.root,
            viewAnnotationOptions {
                geometry(location)
                anchor(ViewAnnotationAnchor.BOTTOM)
                offsetY(offset)
            }
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.selectObservation(null)
        viewModel.setCameraState(mapView.getMapboxMap().cameraState)
    }

    companion object {
        const val DEEP_ZOOM = 18.0
    }
}
