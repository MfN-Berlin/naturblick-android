/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.composable

import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun FloatingActionButton(
    contentDescription: String,
    painter: Painter,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    androidx.compose.material.FloatingActionButton(
        backgroundColor = NaturblickTheme.colors.onSecondaryButtonSecondary,
        contentColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun ExtendedFloatingActionButton(
    text: String, painter: Painter, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    androidx.compose.material.ExtendedFloatingActionButton(
        text = {
            Text(text)
        },
        backgroundColor = NaturblickTheme.colors.onSecondaryButtonSecondary,
        contentColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
        modifier = modifier,
        onClick = onClick,
        icon = {
            Icon(
                painter = painter, contentDescription = text
            )
        })
}