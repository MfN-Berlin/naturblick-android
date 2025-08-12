/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.composable

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NaturblickAlertDialog(
    title: String,
    text: @Composable (() -> Unit)?,
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
        text = text,
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
                    confirm.uppercase(),
                    style = NaturblickTheme.typography.button,
                    color = NaturblickTheme.colors.onSecondaryHighEmphasis
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
                    dismiss.uppercase(),
                    style = NaturblickTheme.typography.button,
                    color = NaturblickTheme.colors.onSecondaryHighEmphasis
                )
            }
        },
        backgroundColor = NaturblickTheme.colors.secondary,
        contentColor = NaturblickTheme.colors.onSecondaryHighEmphasis
    )
}

@Composable
fun SimpleAlertDialog(
    title: String,
    text: String,
    confirm: String,
    dismiss: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    NaturblickAlertDialog(
        title,
        text = {
            Text(
                text,
                style = NaturblickTheme.typography.body2,
                color = NaturblickTheme.colors.onSecondaryMediumEmphasis
            )
        },
        confirm,
        dismiss,
        onDismissRequest,
        onConfirmation
    )

}
