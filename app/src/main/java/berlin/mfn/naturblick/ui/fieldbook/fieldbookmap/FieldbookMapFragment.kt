package berlin.mfn.naturblick.ui.fieldbook.fieldbookmap

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.ObservationDb
import berlin.mfn.naturblick.databinding.FragmentFieldbookMapBinding
import berlin.mfn.naturblick.databinding.ViewObservationPopupBinding
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.BaseActivity
import berlin.mfn.naturblick.ui.data.Group.Companion.getMapIcon
import berlin.mfn.naturblick.ui.fieldbook.CreateAudioObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateImageObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateManualObservation
import berlin.mfn.naturblick.ui.fieldbook.OpenObservation
import berlin.mfn.naturblick.ui.fieldbook.fieldbook.FieldbookActivity
import berlin.mfn.naturblick.ui.fieldbook.fieldbook.FieldbookObservation
import berlin.mfn.naturblick.utils.*
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.util.*
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

class FieldbookMapFragment : MapFragment(), RequestedPermissionCallback {

    private suspend fun getFieldbookObservation(occurenceId: UUID): FieldbookObservation {
        val observation =
            ObservationDb.getDb(requireContext()).operationDao()
                .getObservation(occurenceId)!!
        return FieldbookObservation(
            observation.occurenceId,
            observation.created,
            observation.thumbnailId?.let { MediaThumbnail.remote(it, observation.obsIdent) },
            observation.obsIdent,
            observation.newSpeciesId?.let {
                StrapiDb.getDb(requireContext()).speciesDao().getSpecies(it)
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val popupOffset = resources.getDimension(R.dimen.popup_offset).roundToInt()
        val args: FieldbookMapFragmentArgs by navArgs()
        val viewModel: FieldbookMapViewModel by viewModels() {
            FieldbookMapModelFactory(
                args.occurenceId?.uuid,
                requireActivity().application
            )
        }
        val binding = FragmentFieldbookMapBinding.inflate(inflater, container, false)
        val navController = findNavController()
        val mapView = binding.mapView
        binding.trackingModel = trackingModel
        initializeMapView(mapView, null) {
            val viewAnnotationManager = mapView.viewAnnotationManager
            it.addOnMapClickListener {
                viewAnnotationManager.removeAllViewAnnotations()
                false
            }
            val annotationApi = mapView.annotations
            val pointAnnotationManager =
                annotationApi.createPointAnnotationManager()
            viewModel
                .observationAndSelected
                .observe(viewLifecycleOwner) { (observations, selected) ->
                    pointAnnotationManager.deleteAll()
                    viewAnnotationManager.removeAllViewAnnotations()
                    val annotations: List<PointAnnotationOptions> =
                        observations.mapNotNull { (observation, group) ->
                            observation.coords?.let { coords ->
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
                    pointAnnotationManager.addClickListener { t ->
                        t.getData()?.asString?.let { it ->
                            popup(
                                inflater,
                                container,
                                viewAnnotationManager,
                                t.point,
                                UUID.fromString(it),
                                popupOffset
                            )
                        }
                        true
                    }
                    pointAnnotationManager.create(annotations)
                    selected?.coords?.toPoint()?.let { location ->
                        trackingModel.stopTracking()
                        jumpTo(location, DeepZoom)
                        popup(
                            inflater,
                            container,
                            viewAnnotationManager,
                            location,
                            selected.occurenceId,
                            popupOffset
                        )
                    }
                }

            mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
            binding.toggleGps.setOnClickListener {
                requestLocationPermission.checkPermission(requireContext(), this)
            }
        }
        binding.listButton.setSingleClickListener { _ ->
            (requireActivity() as BaseActivity).supportNavigateUpTo(
                Intent(requireContext(), FieldbookActivity::class.java)
            )
        }
        binding.createObservationAction.setSingleClickListener { _ ->
            navController.navigate(
                FieldbookMapFragmentDirections.navFieldBookMapToNavFieldBookObservation(
                    CreateManualObservation()
                )
            )
        }
        binding.createObservationPhotoAction.setSingleClickListener { _ ->
            navController.navigate(
                FieldbookMapFragmentDirections.navFieldBookMapToNavFieldBookObservation(
                    CreateImageObservation
                )
            )
        }
        binding.createObservationAudioAction.setSingleClickListener { _ ->
            navController.navigate(
                FieldbookMapFragmentDirections.navFieldBookMapToNavFieldBookObservation(
                    CreateAudioObservation
                )
            )
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun popup(
        inflater: LayoutInflater,
        container: ViewGroup?,
        viewAnnotationManager: ViewAnnotationManager,
        location: Geometry,
        occurenceId: UUID,
        offset: Int
    ) {
        lifecycleScope.launch {
            val binding =
                ViewObservationPopupBinding.inflate(
                    inflater,
                    container,
                    false
                )
            binding.observation = getFieldbookObservation(occurenceId)
            binding.root.clipToOutline = true
            binding.button.setSingleClickListener {
                findNavController().navigate(
                    FieldbookMapFragmentDirections
                        .navFieldBookMapToNavFieldBookObservation(
                            OpenObservation(occurenceId)
                        )
                )
            }
            viewAnnotationManager.addViewAnnotation(
                binding.root,
                viewAnnotationOptions {
                    geometry(location)
                    anchor(ViewAnnotationAnchor.BOTTOM)
                    offsetY(offset)
                }
            )
        }
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
}
