/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalDrawer
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import berlin.mfn.naturblick.ui.character.CharacterActivity
import berlin.mfn.naturblick.ui.composable.NaturblickTheme
import berlin.mfn.naturblick.ui.fieldbook.CreateAudioObservation
import berlin.mfn.naturblick.ui.fieldbook.CreateImageObservation
import berlin.mfn.naturblick.ui.fieldbook.ManageObservation
import berlin.mfn.naturblick.ui.fieldbook.fieldbook.FieldbookActivity
import berlin.mfn.naturblick.ui.info.about.AboutActivity
import berlin.mfn.naturblick.ui.info.accessibility.AccessibilityActivity
import berlin.mfn.naturblick.ui.info.account.AccountActivity
import berlin.mfn.naturblick.ui.info.account.AccountActivity.Companion.CLOSE_ON_FINISHED
import berlin.mfn.naturblick.ui.info.feedback.FeedbackActivity
import berlin.mfn.naturblick.ui.info.help.HelpActivity
import berlin.mfn.naturblick.ui.info.imprint.ImprintActivity
import berlin.mfn.naturblick.ui.info.privacy.GeneralPrivacyNoticeActivity
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.ui.info.settings.SettingsActivity
import berlin.mfn.naturblick.ui.species.groups.GroupsActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Settings.check(this, layoutInflater, {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }, {
            startActivity(
                Intent(this, AccountActivity::class.java).apply {
                    putExtra(CLOSE_ON_FINISHED, false)
                })
        })
        setContent {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            NaturblickTheme {
                ModalDrawer(
                    drawerState = drawerState,
                    drawerBackgroundColor = NaturblickTheme.colors.primary,
                    drawerContentColor = NaturblickTheme.colors.onPrimaryHighEmphasis,
                    drawerContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.default_margin)),
                            modifier = Modifier.padding(dimensionResource(R.dimen.default_margin))
                        ) {
                            MenuButton(R.string.menu_start, R.drawable.ic_logo, drawerState) {}
                            MenuButton(
                                R.string.field_book,
                                R.drawable.ic_feldbuch24,
                                drawerState,
                                this@MainActivity::openFieldbook
                            )
                            MenuButton(
                                R.string.record_an_animal,
                                R.drawable.ic_audio24,
                                drawerState,
                                this@MainActivity::recordAnAnimal
                            )
                            MenuButton(
                                R.string.photograph_a_plant,
                                R.drawable.ic_photo24,
                                drawerState,
                                this@MainActivity::photographAPlant
                            )
                            MenuButton(
                                R.string.help,
                                R.drawable.ic_info2,
                                drawerState,
                                this@MainActivity::help
                            )
                            MenuButton(
                                R.string.account,
                                drawerState = drawerState,
                                onClick = this@MainActivity::account
                            )
                            MenuButton(
                                R.string.action_settings,
                                drawerState = drawerState,
                                onClick = this@MainActivity::settings
                            )
                            MenuButton(
                                R.string.feedback,
                                drawerState = drawerState,
                                onClick = this@MainActivity::feedback
                            )
                            MenuButton(
                                R.string.accessibility,
                                drawerState = drawerState,
                                onClick = this@MainActivity::accessibility
                            )
                            MenuButton(
                                R.string.privacy_notice,
                                drawerState = drawerState,
                                onClick = this@MainActivity::privacy
                            )
                            MenuButton(
                                R.string.about,
                                drawerState = drawerState,
                                onClick = this@MainActivity::about
                            )
                        }
                    }) {
                    Box(
                        modifier = Modifier
                            .background(NaturblickTheme.colors.primary)
                            .fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.background_kingfisher),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Scaffold(
                            backgroundColor = Color.Unspecified,
                            contentWindowInsets = WindowInsets.navigationBars,
                            topBar = {
                                Menu(drawerState)
                            }) { padding ->
                            Column(
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = padding.calculateBottomPadding())
                            ) {
                                Spacer(Modifier.weight(0.5f))
                                Image(
                                    painter = painterResource(id = R.drawable.ic_logo),
                                    contentDescription = null,
                                    modifier = Modifier.height(64.dp)
                                )
                                Box(
                                    contentAlignment = Alignment.BottomStart,
                                    modifier = Modifier
                                        .weight(0.5f)
                                        .padding(horizontal = dimensionResource(R.dimen.default_margin))
                                        .fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_museum_logo_inverted),
                                        contentDescription = null,
                                        modifier = Modifier.height(48.dp)
                                    )
                                }
                                Image(
                                    painter = painterResource(id = R.drawable.oval),
                                    contentDescription = null,
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(NaturblickTheme.colors.primary),
                                ) {
                                    Text(
                                        stringResource(R.string.home_identify_animals_and_plants),
                                        style = NaturblickTheme.typography.h6,
                                        color = NaturblickTheme.colors.onPrimaryHighEmphasis
                                    )
                                    Spacer(Modifier.height(dimensionResource(R.dimen.double_margin)))
                                    Row(verticalAlignment = Alignment.Top) {
                                        Spacer(Modifier.weight(0.0625f))
                                        HomeButton(
                                            NaturblickTheme.colors.onPrimaryButtonPrimary,
                                            R.drawable.ic_microphone,
                                            R.string.record_an_animal,
                                            Modifier.weight(0.25f),
                                            this@MainActivity::recordAnAnimal
                                        )
                                        Spacer(Modifier.weight(0.0625f))
                                        HomeButton(
                                            NaturblickTheme.colors.onPrimaryButtonPrimary,
                                            R.drawable.ic_features,
                                            R.string.select_characteristics,
                                            Modifier.weight(0.25f),
                                            this@MainActivity::selectCharacteristics)
                                        Spacer(Modifier.weight(0.0625f))
                                        HomeButton(
                                            NaturblickTheme.colors.onPrimaryButtonPrimary,
                                            R.drawable.ic_photo24,
                                            R.string.photograph_a_plant,
                                            Modifier.weight(0.25f),
                                            this@MainActivity::photographAPlant
                                        )
                                        Spacer(Modifier.weight(0.0625f))
                                    }
                                    Spacer(Modifier.height(dimensionResource(R.dimen.default_margin)))
                                    Row(verticalAlignment = Alignment.Top) {
                                        Spacer(Modifier.weight(0.19f))
                                        HomeButton(
                                            NaturblickTheme.colors.onPrimaryButtonSecondary,
                                            R.drawable.ic_feldbuch24,
                                            R.string.field_book,
                                            Modifier.weight(0.22f),
                                            this@MainActivity::openFieldbook
                                        )
                                        Spacer(Modifier.weight(0.18f))
                                        HomeButton(
                                            NaturblickTheme.colors.onPrimaryButtonSecondary,
                                            R.drawable.ic_specportraits,
                                            R.string.species_portraits,
                                            Modifier.weight(0.22f),
                                            this@MainActivity::portraits)
                                        Spacer(Modifier.weight(0.19f))
                                    }
                                    Spacer(Modifier.height(dimensionResource(R.dimen.default_margin)))
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MenuButton(
        text: Int, icon: Int? = null, drawerState: DrawerState, onClick: () -> Unit
    ) {
        val scope = rememberCoroutineScope()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                        onClick()
                    })
        ) {
            icon?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = NaturblickTheme.colors.onPrimaryHighEmphasis,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.default_margin)))
            }
            Text(
                stringResource(text),
                style = NaturblickTheme.typography.h6,
                color = NaturblickTheme.colors.onPrimaryHighEmphasis
            )
        }
    }

    @Composable
    private fun HomeButton(
        background: Color, icon: Int, text: Int, modifier: Modifier, onClick: () -> Unit
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
            Button(
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = background),
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                onClick = onClick
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = stringResource(id = text),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.half_margin)))
            Text(
                stringResource(id = text),
                style = NaturblickTheme.typography.caption,
                color = NaturblickTheme.colors.onPrimaryHighEmphasis,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun Menu(drawerState: DrawerState) {
        val scope = rememberCoroutineScope()
        IconButton(onClick = {
            scope.launch {
                drawerState.apply {
                    if (isClosed) open() else close()
                }
            }
        }) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = stringResource(R.string.back),
                tint = NaturblickTheme.colors.onPrimaryHighEmphasis
            )
        }
    }

    private fun recordAnAnimal() {
        startActivity(Intent(this, FieldbookActivity::class.java).apply {
            putExtra(ManageObservation.OBSERVATION_ACTION, CreateAudioObservation)
        })
    }

    private fun selectCharacteristics() {
        startActivity(Intent(this, CharacterActivity::class.java))
    }

    private fun photographAPlant() {
        startActivity(Intent(this, FieldbookActivity::class.java).apply {
            putExtra(ManageObservation.OBSERVATION_ACTION, CreateImageObservation)
        })
    }

    private fun openFieldbook() {
        startActivity(Intent(this, FieldbookActivity::class.java))
    }

    private fun portraits() {
        startActivity(Intent(this, GroupsActivity::class.java))
    }
    private fun account() {
        startActivity(Intent(this, AccountActivity::class.java).apply {
            putExtra(CLOSE_ON_FINISHED, false)
        })
    }

    private fun settings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun feedback() {
        startActivity(Intent(this, FeedbackActivity::class.java))
    }

    private fun imprint() {
        startActivity(Intent(this, ImprintActivity::class.java))
    }

    private fun privacy() {
        startActivity(Intent(this, GeneralPrivacyNoticeActivity::class.java))
    }

    private fun about() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun help() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private fun accessibility() {
        startActivity(Intent(this, AccessibilityActivity::class.java))
    }

}
