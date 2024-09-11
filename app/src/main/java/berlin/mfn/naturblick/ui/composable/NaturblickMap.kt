/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.composable

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.composable.NaturblickMap.DEEP_ZOOM
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.MapboxMapScope
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.ViewportStatus
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.data.ViewportStatusChangeReason
import com.mapbox.maps.plugin.viewport.viewport

@Composable
fun ToggleGPSFab(
    locationEnabled: Boolean,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit
) {
    val permissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions.values.any { it }) {
                onToggle(true)
            } else {
                onToggle(false)
            }
        }
    )
    val painter = if (locationEnabled) {
        painterResource(R.drawable.ic_round_gps_fixed_24)
    } else {
        painterResource(R.drawable.ic_round_gps_not_fixed_24)
    }
    val context = LocalContext.current
    FloatingActionButton(
        contentDescription = stringResource(R.string.acc_enable_location_tracking),
        painter = painter,
        modifier = modifier
    ) {
        if (!locationEnabled) {
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onToggle(true)
            } else {
                permissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            onToggle(false)
        }
    }
}

@Composable
fun NaturblickMap(
    locationEnabled: Boolean,
    initialLocation: Point? = null,
    locationCanceled: () -> Unit,
    content: (@Composable @MapboxMapComposable MapboxMapScope.() -> Unit)? = null
) {
    val stateObserver = { from: ViewportStatus, to: ViewportStatus, _: ViewportStatusChangeReason ->
        if (to == ViewportStatus.Idle) {
            locationCanceled()
        }
    }
    MapboxMap(mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(10.447683, 51.163375))
            zoom(5.0)
        }
    },
        modifier = Modifier.fillMaxSize()) {
        content?.let {
            it()
        }
        DisposableMapEffect(locationEnabled) {
            it.location.enabled = locationEnabled
            if (locationEnabled) {
                it.viewport.transitionTo(
                    targetState = it.viewport.makeFollowPuckViewportState(
                        FollowPuckViewportStateOptions.Builder().apply {
                            pitch(null)
                        }.build()
                    ),
                    transition = it.viewport.makeImmediateViewportTransition()
                )
            } else {
                it.viewport.idle()
            }
            onDispose {
                it.location.enabled = false
            }
        }
        DisposableMapEffect {
            it.viewport.addStatusObserver(stateObserver)
            onDispose {
                it.viewport.removeStatusObserver(stateObserver)
            }
        }
        MapEffect(initialLocation) { map ->
            initialLocation?.let {
                map.mapboxMap.setCamera(CameraOptions.Builder().apply {
                    center(it)
                    zoom(DEEP_ZOOM)
                }.build())
            }
        }
    }
}

object NaturblickMap {
    const val DEEP_ZOOM = 18.0
}