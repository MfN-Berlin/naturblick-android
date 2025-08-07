/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.ui.composable.NaturblickTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import java.util.UUID

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ObservationAvatar(observation: FieldbookObservation) {
    GlideImage(
        model = observation.thumbnailRequest,
        contentDescription = null,
        // 4 DP padding of system Icon needs to be taken into account
        modifier = Modifier
            .padding(4.dp)
            .size(dimensionResource(R.dimen.avatar_size) - 8.dp),
        loading = placeholder(R.drawable.placeholder)
    ) {
        it
            .circleCrop()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObservationItem(
    isInSelectionMode: Boolean,
    isSelected: Boolean,
    observation: FieldbookObservation,
    toggle: (UUID) -> Unit,
    open: (UUID) -> Unit
) {
    Row(modifier =
    Modifier
        .fillMaxWidth()
        .combinedClickable(
            onClick = {
                open(observation.occurenceId)
            },
            onLongClick = {
                toggle(observation.occurenceId)
            }
        )
        .padding(dimensionResource(R.dimen.default_margin))
    ) {
        if (isInSelectionMode) {
            IconButton(onClick = { toggle(observation.occurenceId) }) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier.size(dimensionResource(R.dimen.avatar_size))
                    )
                } else {
                    ObservationAvatar(observation)
                }
            }
        } else {
            ObservationAvatar(observation)
        }
        Column(
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.default_margin),
                top = dimensionResource(R.dimen.avatar_text_offset)
            )
        ) {
            observation.nameOrSciname?.let {
                Text(it, style = NaturblickTheme.typography.subtitle1)
            } ?: Text(
                stringResource(R.string.no_species_selected),
                style = NaturblickTheme.typography.subtitle1
            )
            if (observation.name != null) {
                observation.species?.sciname?.let {
                    Text(
                        it,
                        style = NaturblickTheme.typography.subtitle3,
                        color = NaturblickTheme.colors.onSecondarySignalLow
                    )
                }
            }
            Text(
                observation.localDateTimeString,
                style = NaturblickTheme.typography.synonym,
                color = NaturblickTheme.colors.onSecondaryHighEmphasis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ObservationList(
    selection: List<UUID>,
    observations: List<FieldbookObservation>,
    open: (UUID) -> Unit,
    toggle: (UUID) -> Unit
) {
    val state = rememberLazyListState()
    LazyColumn(
        state = state,
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        itemsIndexed(observations, key = { _, o -> o.occurenceId }) { _, observation ->
            Column(modifier = Modifier.animateItemPlacement()) {
                ObservationItem(
                    selection.isNotEmpty(),
                    selection.contains(observation.occurenceId),
                    observation,
                    toggle,
                    open
                )
                Divider(
                    thickness = dimensionResource(R.dimen.hr_height),
                    startIndent = dimensionResource(R.dimen.avatar_size) + dimensionResource(
                        R.dimen.default_margin
                    ) * 2
                )
            }
        }
    }
    /* Scroll to first item when it changes */
    LaunchedEffect(key1 = observations.firstOrNull()) {
        if (observations.isNotEmpty()) {
            state.animateScrollToItem(0)
        }
    }
}