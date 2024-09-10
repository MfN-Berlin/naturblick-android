/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.composable.NaturblickMap
import berlin.mfn.naturblick.ui.composable.NaturblickTheme
import berlin.mfn.naturblick.ui.data.Group
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Point
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.extension.compose.DisposableMapEffect
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.viewannotation.annotationAnchor
import com.mapbox.maps.viewannotation.geometry
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import java.util.UUID

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun MapPopup(observation: FieldbookObservation, open: (UUID) -> Unit) {
    Box(modifier = Modifier.size(dimensionResource(R.dimen.observation_popup_size))) {
        GlideImage(
            model = observation.thumbnailRequest,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x00000000),
                            Color(0x7FFFFFFF),
                            Color.White
                        )
                    )
                )
                .fillMaxSize()
        ) {
            val title =
                observation.nameOrSciname ?: stringResource(id = R.string.no_species_selected)
            Text(title, style = NaturblickTheme.typography.subtitle1, color = Color.Black)
            Text(
                observation.localDateTimeString,
                style = NaturblickTheme.typography.subtitle3,
                color = Color.Black
            )
            Button(
                onClick = {
                    open(observation.occurenceId)
                },
                colors = NaturblickTheme.buttonColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.default_margin))
            ) {
                Text(
                    stringResource(id = R.string.view_details),
                    style = NaturblickTheme.typography.button
                )
            }
        }
    }
}

@Composable
fun ObservationMap(
    locationEnabled: Boolean,
    observations: List<FieldbookObservation>,
    initialObservation: UUID?,
    activeObservation: UUID?,
    onClick: (Point) -> Boolean,
    locationCanceled: () -> Unit,
    open: (UUID) -> Unit,
    select: (UUID?) -> Unit
) {
    var initialLocation: Point? by remember { mutableStateOf(null) }
    NaturblickMap(
        initialLocation = initialLocation,
        locationEnabled = locationEnabled,
        locationCanceled = locationCanceled
    ) {
        activeObservation?.let { occurenceId ->
            observations.find { occurenceId == it.occurenceId }?.let { observation ->
                observation.coords?.let { coords ->
                    ViewAnnotation(options = viewAnnotationOptions {
                        geometry(Point.fromLngLat(coords.lon, coords.lat))
                        annotationAnchor {
                            anchor(ViewAnnotationAnchor.BOTTOM)
                        }
                    }) {
                        MapPopup(observation, open)
                    }
                }
            }
        }

        PointAnnotationGroup(
            annotations = observations.mapNotNull { observation ->
                observation.coords?.let { coords ->
                    PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(coords.lon, coords.lat))
                        .withIconImage(
                            Group.getMapIcon(
                                LocalContext.current,
                                observation.species?.group
                            )
                        )
                        .withData(JsonPrimitive(observation.occurenceId.toString()))
                }
            }, annotationConfig = AnnotationConfig(
                annotationSourceOptions = AnnotationSourceOptions(
                    clusterOptions = ClusterOptions(
                        circleRadiusExpression = literal(16),
                        colorLevels = listOf(
                            Pair(50, colorResource(R.color.sleep_900).toArgb()),
                            Pair(10, colorResource(R.color.sleep_400).toArgb()),
                            Pair(0, colorResource(R.color.sleep_100).toArgb())
                        )
                    )
                )
            ),
            onClick = { annotation ->
                annotation.getData()?.asString?.let { occurenceIdString ->
                    UUID.fromString(occurenceIdString)?.let { occurenceId ->
                        select(occurenceId)
                    }
                }
                true
            }
        )
        DisposableMapEffect {
            it.mapboxMap.addOnMapClickListener(onClick)
            onDispose {
                it.mapboxMap.removeOnMapClickListener(onClick)
            }
        }
    }
    // Set initial observation if available
    LaunchedEffect(observations) {
        observations.find { it.occurenceId == initialObservation }?.let { observation ->
            observation.coords?.let {
                initialLocation = Point.fromLngLat(it.lon, it.lat)
            }
        }
    }
}
