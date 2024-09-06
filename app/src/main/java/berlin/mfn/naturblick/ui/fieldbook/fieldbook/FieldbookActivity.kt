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
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.SyncWorker
import berlin.mfn.naturblick.ui.composable.SimpleAlertDialog
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
            val selection = remember {
                mutableStateListOf<UUID>()
            }
            var openDeleteDialog by remember { mutableStateOf(false) }
            NaturblickTheme {
                Scaffold(
                    topBar = {
                        TopBar(
                            selectionCount = selection.size,
                            observationCount = observations.size,
                            query = model.query,
                            updateQuery = {
                                model.updateQuery(it)
                            },
                            cancelSelection = {
                                selection.clear()
                            },
                            deleteSelection = {
                                openDeleteDialog = true
                            })
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
                        ObservationList(selection, observations) { occurenceId ->
                            if (selection.contains(occurenceId)) {
                                selection.remove(occurenceId)
                            } else {
                                selection.add(occurenceId)
                            }
                        }
                        PullRefreshIndicator(
                            refreshing = isRefreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
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
            title = stringResource(R.string.delete_question_observations),
            text = stringResource(R.string.delete_question_observations_message, deletionCount),
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
        toggle: (UUID) -> Unit
    ) {
        Row(modifier =
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    startActivity(
                        Intent(this, ObservationActivity::class.java)
                            .putExtra(
                                OBSERVATION_ACTION,
                                OpenObservation(observation.occurenceId)
                            )
                    )
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
                    style = NaturblickTheme.typography.subtitle3,
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
        toggle: (UUID) -> Unit
    ) {
        val state = rememberLazyListState()
        LazyColumn(
            state = state
        ) {
            itemsIndexed(observations, key = { _, o -> o.occurenceId }) { _, observation ->
                Column(modifier = Modifier.animateItemPlacement()) {
                    ObservationItem(
                        selection.isNotEmpty(),
                        selection.contains(observation.occurenceId),
                        observation,
                        toggle
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

    @Composable
    fun TopBar(
        selectionCount: Int,
        observationCount: Int,
        query: String,
        updateQuery: (query: String) -> Unit,
        cancelSelection: () -> Unit,
        deleteSelection: () -> Unit
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
                        }
                        else {
                            cancelSelection()
                        }
                    }
                }
            },
            actions = {
                if (!search && !isInSelectionMode) {
                    IconButton(onClick = {
                        search = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search)
                        )
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
                }
            },
            backgroundColor = NaturblickTheme.colors.primary,
            contentColor = NaturblickTheme.colors.onPrimary,
            title = {
                if(isInSelectionMode) {
                    Text("$selectionCount")
                } else {
                    if (!search) {
                        Text(stringResource(R.string.field_book_title, observationCount))
                    } else {
                        SearchField(stringResource(R.string.search_hint), query, updateQuery) {
                            search = false
                        }
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