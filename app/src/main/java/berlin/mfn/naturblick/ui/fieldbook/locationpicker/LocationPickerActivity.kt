/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.locationpicker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.allViews
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.Coordinates
import berlin.mfn.naturblick.ui.composable.ExtendedFloatingActionButton
import berlin.mfn.naturblick.ui.composable.NaturblickMap
import berlin.mfn.naturblick.ui.composable.NaturblickTheme
import berlin.mfn.naturblick.ui.composable.ToggleGPSFab
import com.mapbox.geojson.Point
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.compose.MapEffect

class LocationPickerActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialLocation = IntentCompat.getParcelableExtra(
            intent,
            INITIAL_LOCATION,
            Coordinates::class.java
        )?.let {
            Point.fromLngLat(it.lon, it.lat)
        }

        setContent {
            NaturblickTheme {
                var locationEnabled by remember { mutableStateOf(false) }
                var setLocation by remember { mutableStateOf(false) }
                Scaffold(floatingActionButton = {
                    ToggleGPSFab(
                        locationEnabled = locationEnabled
                    ) {
                        locationEnabled = it
                    }
                }, topBar = {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.location))
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                setResult(RESULT_CANCELED)
                                finish()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        },
                        backgroundColor = NaturblickTheme.colors.primary,
                        contentColor = NaturblickTheme.colors.onPrimary,
                    )
                }) {
                    Box {
                        NaturblickMap(
                            initialLocation = initialLocation,
                            locationEnabled = locationEnabled,
                            locationCanceled = {
                                locationEnabled = false
                            }) {
                            MapEffect(setLocation) { map ->
                                if (setLocation) {
                                    map.allViews.find { it is ImageView }?.let { dropPinView ->
                                        val position = map.mapboxMap.coordinateForPixel(
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
                                        setResult(Activity.RESULT_OK, intent)
                                        finish()
                                    }
                                } else {
                                    val dropPinView = dropPin()
                                    map.addView(dropPinView)
                                }
                            }
                        }
                        ExtendedFloatingActionButton(
                            text = stringResource(R.string.select_location),
                            painter = painterResource(
                                R.drawable.ic_baseline_edit_location_24
                            ),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(dimensionResource(R.dimen.default_margin))
                        ) {
                            setLocation = true
                        }
                    }
                }
            }
        }
    }

    private fun dropPin(): ImageView {
        val dropPinView = ImageView(this)
        dropPinView.setImageResource(R.drawable.ic_baseline_edit_location_24)
        dropPinView.setColorFilter(ContextCompat.getColor(this, R.color.secondary_200))
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

    companion object {
        const val TAG = "LocationPickerFragment"
        const val PICKED_LOCATION = "picked_location"
        const val INITIAL_LOCATION = "initial_location"
    }
}
