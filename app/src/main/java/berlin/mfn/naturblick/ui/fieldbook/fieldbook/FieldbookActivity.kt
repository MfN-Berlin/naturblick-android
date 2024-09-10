/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.SyncWorker
import berlin.mfn.naturblick.ui.composable.SimpleAlertDialog
import berlin.mfn.naturblick.ui.composable.FloatingActionButton
import berlin.mfn.naturblick.ui.composable.NaturblickTheme
import berlin.mfn.naturblick.ui.composable.SearchField
import berlin.mfn.naturblick.ui.composable.ToggleGPSFab
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
import berlin.mfn.naturblick.ui.fieldbook.observation.ObservationActivity
import berlin.mfn.naturblick.ui.info.account.AccountActivity
import berlin.mfn.naturblick.ui.info.settings.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.UUID

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

    private fun openObservation(occurenceId: UUID) {
        startActivity(
            Intent(this, ObservationActivity::class.java).putExtra(
                OBSERVATION_ACTION,
                OpenObservation(occurenceId)
            )
        )
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
        val initialObservation =
            IntentCompat.getParcelableExtra(intent, OccurenceId, ParcelUuid::class.java)?.uuid
        setContent {
            val observations by model.observationsFlow.collectAsState(emptyList())
            val selection = remember {
                mutableStateListOf<UUID>()
            }
            var openDeleteDialog by remember { mutableStateOf(false) }
            var isMapView by remember { mutableStateOf(initialObservation != null) }
            var activeObservation by remember { mutableStateOf(initialObservation) }
            var locationEnabled by remember { mutableStateOf(false) }
            NaturblickTheme {
                Scaffold(
                    topBar = {
                        TopBar(
                            selectionCount = selection.size,
                            observationCount = observations.size,
                            query = model.query,
                            isMapView = isMapView,
                            updateQuery = {
                                model.updateQuery(it)
                            },
                            cancelSelection = {
                                selection.clear()
                            },
                            deleteSelection = {
                                openDeleteDialog = true
                            },
                            toggleMapView = {
                                isMapView = !isMapView
                            })
                    },
                    floatingActionButton = {
                        Column {
                            if(isMapView) {
                                ToggleGPSFab(
                                    locationEnabled, modifier = Modifier.padding(
                                        bottom = dimensionResource(
                                            R.dimen.default_margin
                                        )
                                    )
                                ) {
                                    locationEnabled = it
                                }
                            }
                            FloatingActionButton(
                                contentDescription = stringResource(R.string.create_observation),
                                painter = painterResource(R.drawable.ic_add)
                            ) {
                                createObservation()
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
                        if (!isMapView) {
                            ObservationList(selection, observations, open = ::openObservation) {
                                if (selection.contains(it)) {
                                    selection.remove(it)
                                } else {
                                    selection.add(it)
                                }
                            }
                            PullRefreshIndicator(
                                refreshing = isRefreshing,
                                state = pullRefreshState,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        } else {
                            ObservationMap(
                                locationEnabled,
                                observations,
                                initialObservation,
                                activeObservation,
                                locationCanceled = {
                                    locationEnabled = false
                                },
                                onClick = { _ ->
                                    if(activeObservation != null) {
                                        activeObservation = null
                                        true
                                    } else {
                                        false
                                    }
                                },
                                open = ::openObservation
                            ) { selected ->
                                activeObservation = selected
                            }
                        }
                    }
                    when {
                        openDeleteDialog ->
                            DeleteDialog(
                                deletionCount = selection.size,
                                onDismissRequest = {
                                    openDeleteDialog = false
                                },
                                onConfirmation = {
                                    model.deleteObservations(selection)
                                    selection.clear()
                                    openDeleteDialog = false
                                }
                            )
                    }
                }
            }
        }
    }

    @Composable
    fun DeleteDialog(deletionCount: Int, onDismissRequest: () -> Unit, onConfirmation: () -> Unit) {
        SimpleAlertDialog(
            title = pluralStringResource(R.plurals.delete_question_observations, deletionCount),
            text = pluralStringResource(
                R.plurals.delete_question_observations_message,
                deletionCount,
                deletionCount
            ),
            confirm = stringResource(R.string.delete),
            dismiss = stringResource(R.string.cancel),
            onDismissRequest = onDismissRequest,
            onConfirmation = onConfirmation
        )

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

    @Composable
    fun MapAction(isMapView: Boolean, onClick: () -> Unit) {
        IconButton(onClick) {
            if (isMapView)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = stringResource(R.string.field_book_map)
                )
            else
                Icon(
                    painter = painterResource(R.drawable.ic_map),
                    contentDescription = stringResource(R.string.field_book_map)
                )
        }
    }

    @Composable
    fun TopBar(
        selectionCount: Int,
        observationCount: Int,
        isMapView: Boolean,
        query: String,
        updateQuery: (query: String) -> Unit,
        cancelSelection: () -> Unit,
        deleteSelection: () -> Unit,
        toggleMapView: () -> Unit
    ) {
        var search by remember { mutableStateOf(false) }
        val isInSelectionMode = selectionCount > 0
        TopAppBar(
            navigationIcon = {
                NavigationIcon {
                    if (!search && !isInSelectionMode) {
                        this.setResult(RESULT_OK)
                        this.finish()
                    } else {
                        if (search) {
                            search = false
                            updateQuery("")
                        } else {
                            cancelSelection()
                        }
                    }
                }
            },
            actions = {
                // Override default alpha so that icons are opaque
                CompositionLocalProvider(LocalContentAlpha provides 1f) {
                    if (!search && !isInSelectionMode) {
                        IconButton(onClick = {
                            search = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.search)
                            )
                        }
                        MapAction(isMapView) {
                            toggleMapView()
                        }
                    } else if (isInSelectionMode) {
                        IconButton(onClick = {
                            deleteSelection()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                        IconButton(onClick = {
                            cancelSelection()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    } else {
                        MapAction(isMapView) {
                            toggleMapView()
                        }
                    }
                }
            },
            backgroundColor = NaturblickTheme.colors.primary,
            contentColor = NaturblickTheme.colors.onPrimary,
            title = {
                if (isInSelectionMode) {
                    Text("$selectionCount")
                } else {
                    if (!search) {
                        Text(
                            pluralStringResource(
                                R.plurals.field_book_title,
                                observationCount,
                                observationCount
                            )
                        )
                    } else {
                        SearchField(stringResource(R.string.search_hint), query, updateQuery) {
                            search = false
                        }
                    }
                }
            }
        )
    }


    companion object {
        const val OccurenceId = "occurence_id"
    }
}