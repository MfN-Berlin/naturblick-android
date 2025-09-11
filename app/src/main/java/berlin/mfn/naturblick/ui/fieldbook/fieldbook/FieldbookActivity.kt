/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.IntentCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.compose.AndroidFragment
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.backend.SyncWorker
import berlin.mfn.naturblick.ui.composable.FloatingActionButton
import berlin.mfn.naturblick.ui.composable.NaturblickTheme
import berlin.mfn.naturblick.ui.composable.SearchField
import berlin.mfn.naturblick.ui.composable.SimpleAlertDialog
import berlin.mfn.naturblick.ui.composable.ToggleGPSFab
import berlin.mfn.naturblick.ui.data.UiGroup
import berlin.mfn.naturblick.ui.data.getLabel
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

const val SHOW_FILTER_THRESHOLD = 2

class FieldbookActivity : FragmentActivity() {
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
        val initialObservation =
            IntentCompat.getParcelableExtra(intent, OCCURENCE_ID, ParcelUuid::class.java)?.uuid
        val model: FieldbookViewModel by viewModels {
            FieldbookViewModel.Factory
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
            val selectableGroups by model.selectableGroupsFlow.collectAsState(emptyList())
            val observations by model.observationsFlow.collectAsState(emptyList())
            val selection = remember {
                mutableStateListOf<UUID>()
            }
            var openDeleteDialog by remember { mutableStateOf(false) }
            var isMapView by remember { mutableStateOf(initialObservation != null) }

            var openGroupsDialog by remember { mutableStateOf(false) }
            val showGroupsFilter = selectableGroups.size > SHOW_FILTER_THRESHOLD

            NaturblickTheme {
                Scaffold(
                    contentWindowInsets = WindowInsets.systemBars,
                    topBar = {
                        TopBar(
                            selectionCount = selection.size,
                            observationCount = if (!isMapView) observations.size else observations.filter { it.coords != null }.size,
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
                                if (!isMapView) {
                                    model.stopTracking()
                                }
                            },
                            updateOpenGroupsDialog = { openGroupsDialog = !openGroupsDialog },
                            group = model.group,
                            showGroupsFilter = showGroupsFilter
                        )
                    },
                    floatingActionButton = {
                        Column {
                            if (isMapView) {
                                ToggleGPSFab(
                                    model.locationEnabled, modifier = Modifier.padding(
                                        bottom = dimensionResource(
                                            R.dimen.default_margin
                                        )
                                    )
                                ) {
                                    if (it) {
                                        model.startTracking()
                                    } else {
                                        model.stopTracking()
                                    }
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
                            AndroidFragment<FieldbookMapFragment>()
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
                    if (openGroupsDialog) {
                        GroupFilterDialog(
                            selectableGroups,
                            model.group,
                            onDismiss = { openGroupsDialog = !openGroupsDialog },
                            onConfirm = { selectedGroup ->
                                model.updateGroup(selectedGroup)
                                openGroupsDialog = !openGroupsDialog
                            })
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
    fun FilterAction(
        isFiltered: Boolean,
        updateOpenGroupsDialog: () -> Unit
    ) {
        IconButton(onClick = {
            updateOpenGroupsDialog()
        }) {
            if (isFiltered) {
                Icon(
                    painter = painterResource(R.drawable.filter_alt_off_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                    contentDescription = stringResource(id = R.string.filter)
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.filter_alt_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                    contentDescription = stringResource(id = R.string.filter)
                )
            }
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
    fun GroupFilterDialog(
        selectableGroups: List<UiGroup>,
        group: UiGroup,
        onDismiss: () -> Unit,
        onConfirm: (selectedGroup: UiGroup) -> Unit
    ) {
        var newSelectedGroup by remember { mutableStateOf(group) }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NaturblickTheme.colors.secondary
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.filter),
                        style = NaturblickTheme.typography.h6,
                        color = NaturblickTheme.colors.onSecondaryHighEmphasis
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 310.dp)
                    ) {
                        items(items = selectableGroups) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = newSelectedGroup == item,
                                        onClick = { newSelectedGroup = item },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = newSelectedGroup == item,
                                    onClick = { newSelectedGroup = item },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = NaturblickTheme.colors.onSecondaryHighEmphasis,
                                        unselectedColor = NaturblickTheme.colors.onSecondaryMediumEmphasis
                                    )
                                )

                                Text(
                                    text = applicationContext.getLabel(item.label),
                                    style = NaturblickTheme.typography.body1,
                                    color = NaturblickTheme.colors.onSecondaryHighEmphasis
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                stringResource(R.string.cancel),
                                style = NaturblickTheme.typography.button,
                                color = NaturblickTheme.colors.onSecondaryButtonPrimary
                            )
                        }
                        TextButton(onClick = { onConfirm(newSelectedGroup) }) {
                            Text(
                                stringResource(R.string.filter_ok),
                                style = NaturblickTheme.typography.button,
                                color = NaturblickTheme.colors.onSecondaryButtonPrimary
                            )
                        }
                    }
                }
            }
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
        toggleMapView: () -> Unit,
        updateOpenGroupsDialog: () -> Unit,
        group: UiGroup?,
        showGroupsFilter: Boolean
    ) {
        var search by remember { mutableStateOf(false) }
        val isInSelectionMode = selectionCount > 0
        TopAppBar(
            windowInsets = WindowInsets.statusBars,
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
                        if (showGroupsFilter) {
                            FilterAction(
                                group?.id != UiGroup.Companion.ALL_GROUP_ID,
                                updateOpenGroupsDialog = updateOpenGroupsDialog
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
                        if (showGroupsFilter) {
                            FilterAction(
                                group?.id != UiGroup.Companion.ALL_GROUP_ID,
                                updateOpenGroupsDialog = updateOpenGroupsDialog
                            )
                        }
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
                        SearchField(
                            stringResource(R.string.search_hint),
                            query,
                            updateQuery
                        ) {
                            search = false
                        }
                    }
                }
            }
        )
    }

    companion object {
        const val OCCURENCE_ID = "occurence_id"
    }
}