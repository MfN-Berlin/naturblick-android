/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.backend.Coordinates
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location

abstract class MapFragment : Fragment() {
    protected lateinit var trackingModel: TrackingViewModel
    private lateinit var mapView: MapView

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        jumpTo(it)
    }
    private val onMoveListener: OnMoveListener = object : OnMoveListener {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(detector: MoveGestureDetector) {
            trackingModel.stopTracking()
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    protected fun initializeMapView(
        view: MapView,
        initialLocation: Coordinates?,
        done: (map: MapboxMap) -> Unit
    ) {
        mapView = view
        val map = mapView.getMapboxMap()
        map.loadStyleUri(
            BuildConfig.STYLE_URL
        ) {
            trackingModel.setTrackingStartedCallback(::startTracking)
            trackingModel.setTrackingStoppedCallback(::stopTracking)
            map.gesturesPlugin { addOnMoveListener(onMoveListener) }
            if (initialLocation != null) {
                jumpTo(initialLocation.toPoint(), DeepZoom)
            }
            mapView.location.updateSettings {
                enabled = true
            }
            done(map)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModels<TrackingViewModel>()
        trackingModel = viewModel
    }

    protected fun jumpTo(location: Point, zoom: Double? = null) {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder().apply {
                center(
                    location
                )
                zoom?.let {
                    zoom(it)
                }
            }.build()
        )
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        mapView.location
            .addOnIndicatorPositionChangedListener(
                onIndicatorPositionChangedListener
            )
    }

    private fun stopTracking() {
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    override fun onStop() {
        super.onStop()
        stopTracking()
    }

    companion object {
        const val DeepZoom = 18.0
    }
}
