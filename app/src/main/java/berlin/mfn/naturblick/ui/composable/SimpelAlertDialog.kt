/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.composable

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SimpleAlertDialog(
    title: String,
    text: String,
    confirm: String,
    dismiss: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    androidx.compose.material.AlertDialog(
        title = {
            Text(
                title,
                style = NaturblickTheme.typography.h6,
                color = NaturblickTheme.colors.onSecondaryHighEmphasis
            )
        },
        text = {
            Text(
                text,
                style = NaturblickTheme.typography.body2,
                color = NaturblickTheme.colors.onSecondaryMediumEmphasis
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(
                    confirm,
                    style = NaturblickTheme.typography.button,
                    color = NaturblickTheme.colors.onSecondaryButtonPrimary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(
                    dismiss,
                    style = NaturblickTheme.typography.button,
                    color = NaturblickTheme.colors.onSecondaryButtonPrimary
                )
            }
        }
    )
}