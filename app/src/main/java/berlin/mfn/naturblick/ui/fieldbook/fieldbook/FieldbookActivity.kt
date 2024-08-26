/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.SyncWorker
import berlin.mfn.naturblick.ui.composable.ExtendedFloatingActionButton
import berlin.mfn.naturblick.ui.composable.FloatingActionButton
import berlin.mfn.naturblick.ui.composable.NaturblickTheme
import berlin.mfn.naturblick.ui.composable.SearchField
import berlin.mfn.naturblick.ui.fieldbook.CreateAudioObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateImageFromGalleryObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateImageObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateManualObservation
import berlin.mfn.naturblick.ui.fieldbook.ManageObservation
import berlin.mfn.naturblick.ui.fieldbook.ManageObservation.OBSERVATION_ACTION
import berlin.mfn.naturblick.ui.fieldbook.ManageObservationCanceled
import berlin.mfn.naturblick.ui.fieldbook.ManageObservationCreated
import berlin.mfn.naturblick.ui.fieldbook.ManageObservationFinished
import berlin.mfn.naturblick.ui.fieldbook.ObservationAction
import berlin.mfn.naturblick.ui.fieldbook.OpenObservation
import berlin.mfn.naturblick.ui.fieldbook.fieldbookmap.FieldbookMapActivity
import berlin.mfn.naturblick.ui.fieldbook.observation.ObservationActivity
import berlin.mfn.naturblick.ui.info.account.AccountActivity
import berlin.mfn.naturblick.ui.info.settings.Settings
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FieldbookActivity : ComponentActivity() {
    private val manageObservation = registerForActivityResult(ManageObservation) {
        when (it) {
            is ManageObservationCanceled -> {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }

            is ManageObservationFinished -> {
                SyncWorker.triggerBackgroundSync(applicationContext, ::onSignedOut)
            }

            is ManageObservationCreated -> {
                SyncWorker.triggerBackgroundSync(applicationContext, ::onSignedOut)
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val model: FieldbookViewModel by viewModels {
            FieldbookViewModelFactory(application)
        }
        val action = IntentCompat.getParcelableExtra(
            intent,
            OBSERVATION_ACTION,
            ObservationAction::class.java
        )
        if (action != null && !model.launched) {
            model.launched = true
            manageObservation.launch(action)
        } else {
            SyncWorker.triggerBackgroundSync(applicationContext, ::onSignedOut)
        }
        setContent {
            val observations by model.observationsFlow.collectAsState(emptyList())
            NaturblickTheme {
                Scaffold(
                    topBar = {
                        TopBar(model.query) {
                            model.updateQuery(it)
                        }
                    },
                    floatingActionButton = {
                        Column(horizontalAlignment = Alignment.End) {
                            FloatingActionButton(
                                contentDescription = stringResource(R.string.create_observation),
                                painter = painterResource(R.drawable.ic_add),
                                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.default_margin))
                            ) {
                                createObservation()
                            }
                            ExtendedFloatingActionButton(
                                text = stringResource(R.string.field_book_map),
                                painter = painterResource(R.drawable.ic_map)
                            ) {
                                startActivity(
                                    Intent(
                                        this@FieldbookActivity,
                                        FieldbookMapActivity::class.java
                                    )
                                )
                            }
                        }
                    }
                ) { _ ->
                    val isRefreshing = model.refreshState
                    val pullRefreshState =
                        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
                            if (Settings.canSync(this)) {
                                model.refresh()
                            } else {
                                onSignedOut()
                            }
                        })
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(state = pullRefreshState)
                    ) {
                        ObservationList(observations)
                        PullRefreshIndicator(
                            refreshing = isRefreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun NavigationIcon(onBack: () -> Unit) {
        IconButton(onClick = {
            onBack()
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back)
            )
        }
    }

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun ObservationItem(observation: FieldbookObservation, modifier: Modifier = Modifier) {
        Row(modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .clickable {
                    startActivity(
                        Intent(this, ObservationActivity::class.java)
                            .putExtra(
                                OBSERVATION_ACTION,
                                OpenObservation(observation.occurenceId)
                            )
                    )
                }
                .padding(dimensionResource(R.dimen.default_margin)))
        ) {
            GlideImage(
                model = observation.thumbnailRequest,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.avatar_size)),
                loading = placeholder(R.drawable.placeholder)
            ) {
                it
                    .circleCrop()

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
                    style = NaturblickTheme.typography.subtitle3,
                    color = NaturblickTheme.colors.onSecondaryHighEmphasis
                )
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ObservationList(observations: List<FieldbookObservation>) {
        val state = rememberLazyListState()
        LazyColumn(
            state = state
        ) {
            itemsIndexed(observations, key = { _, o -> o.occurenceId }) { _, observation ->
                Column(modifier = Modifier.animateItemPlacement()) {
                    ObservationItem(observation)
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

    @Composable
    fun TopBar(query: String, updateQuery: (query: String) -> Unit) {
        var search by remember { mutableStateOf(false) }
        TopAppBar(
            navigationIcon = {
                NavigationIcon {
                    if (!search) {
                        this.setResult(RESULT_OK)
                        this.finish()
                    } else {
                        search = false
                    }
                }
            },
            actions = {
                if (!search) {
                    IconButton(onClick = {
                        search = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                }
            },
            backgroundColor = NaturblickTheme.colors.primary,
            contentColor = NaturblickTheme.colors.onPrimary,
            title = {
                if (!search)
                    Text(stringResource(R.string.field_book))
                else {
                    SearchField(stringResource(R.string.search_hint), query, updateQuery) {
                        search = false
                    }
                }
            }
        )
    }

    private fun onSignedOut() {
        MaterialAlertDialogBuilder(this, R.style.Naturblick_MaterialComponents_Dialog_Alert).apply {
            setMessage(R.string.error_signed_out)
            setPositiveButton(R.string.yes) { _, _ ->
                startActivity(Intent(applicationContext, AccountActivity::class.java))
            }
            setNegativeButton(
                R.string.no
            ) { _, _ ->
                Settings.clearEmail(context)
                SyncWorker.triggerBackgroundSync(applicationContext, ::onSignedOut)
            }
        }.show()
    }

    private fun createObservation() {
        MaterialAlertDialogBuilder(
            this,
            R.style.Naturblick_MaterialComponents_Dialog_Alert
        ).apply {
            setTitle(R.string.create_observation)
            setItems(R.array.create_observation_options) { _, chosen ->
                when (chosen) {
                    0 -> manageObservation.launch(
                        CreateManualObservation()
                    )

                    1 -> manageObservation.launch(
                        CreateImageObservation
                    )

                    2 -> manageObservation.launch(
                        CreateAudioObservation
                    )

                    3 -> if (Settings.hasSeenImportInfo(this@FieldbookActivity)) {
                        manageObservation.launch(
                            CreateImageFromGalleryObservation
                        )
                    } else {
                        MaterialAlertDialogBuilder(
                            this@FieldbookActivity,
                            R.style.Naturblick_MaterialComponents_Dialog_Alert
                        ).apply {
                            setTitle(R.string.import_info_title)
                            setMessage(R.string.import_info)
                            setPositiveButton(R.string.continue_str) { _, _ ->
                                Settings.didSeeImportInfo(this@FieldbookActivity)
                                manageObservation.launch(
                                    CreateImageFromGalleryObservation
                                )
                            }
                            setOnCancelListener {
                            }
                        }.show()
                    }
                }
            }
            setNeutralButton(R.string.cancel) { _, _ ->
            }
        }.show()
    }

}