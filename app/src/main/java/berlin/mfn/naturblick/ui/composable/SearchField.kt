/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import berlin.mfn.naturblick.R

@Composable
fun SearchField(searchHint: String, query: String, updateQuery: (query: String) -> Unit, closeSearch: () -> Unit) {
    val textFieldFocus = remember { FocusRequester() }
    TextField(
        singleLine = true,
        value = query,
        onValueChange = {
            updateQuery(it)
        },
        placeholder = {
            Row {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search)
                )
                Text(
                    searchHint,
                    style = NaturblickTheme.typography.h6
                )
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = NaturblickTheme.colors.primary,
            textColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
            disabledTextColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
            cursorColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
            placeholderColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
            trailingIconColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
            focusedIndicatorColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
            unfocusedIndicatorColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
            disabledIndicatorColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
        ),
        textStyle = NaturblickTheme.typography.h6,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        trailingIcon = {
            IconButton(onClick = {
                if (query.isNotBlank())
                    updateQuery("")
                else
                    closeSearch()
            }) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = stringResource(R.string.clear)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                bottom = dimensionResource(
                    R.dimen.small_margin
                )
            )
            .focusRequester(textFieldFocus)
    )
    LaunchedEffect(Unit) {
        textFieldFocus.requestFocus()
    }
}