/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Settings.check(
            this, layoutInflater, {
                setResult(Activity.RESULT_CANCELED)
                finish()
            },
            {
                startActivity(
                    Intent(this, AccountActivity::class.java).apply {
                        putExtra(CLOSE_ON_FINISHED, false)
                    }
                )
            }
        )
        setContent {
            NaturblickTheme {
                Scaffold(contentWindowInsets = WindowInsets.navigationBars) { padding ->
                    Box(
                        modifier = Modifier
                            .background(NaturblickTheme.colors.primary)
                            .padding(bottom = padding.calculateBottomPadding())
                            .fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.background_kingfisher),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Column(
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Spacer(Modifier.weight(0.5f))
                                Image(painter = painterResource(id = R.drawable.ic_logo),
                                    contentDescription = null, modifier = Modifier.height(64.dp))
                            Box(contentAlignment = Alignment.BottomStart, modifier = Modifier
                                .weight(0.5f)
                                .padding(horizontal = dimensionResource(R.dimen.default_margin))
                                .fillMaxWidth()) {
                                Image(painter = painterResource(id = R.drawable.ic_museum_logo_inverted),
                                    contentDescription = null, modifier = Modifier.height(48.dp))
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
                                        Modifier.weight(0.25f)
                                    )
                                    Spacer(Modifier.weight(0.0625f))
                                    HomeButton(
                                        NaturblickTheme.colors.onPrimaryButtonPrimary,
                                        R.drawable.ic_features,
                                        R.string.select_characteristics,
                                        Modifier.weight(0.25f)
                                    )
                                    Spacer(Modifier.weight(0.0625f))
                                    HomeButton(
                                        NaturblickTheme.colors.onPrimaryButtonPrimary,
                                        R.drawable.ic_photo24,
                                        R.string.photograph_a_plant,
                                        Modifier.weight(0.25f)
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
                                        Modifier.weight(0.22f)
                                    )
                                    Spacer(Modifier.weight(0.18f))
                                    HomeButton(
                                        NaturblickTheme.colors.onPrimaryButtonSecondary,
                                        R.drawable.ic_specportraits,
                                        R.string.species_portraits,
                                        Modifier.weight(0.22f)
                                    )
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

    @Composable
    private fun HomeButton(background: Color, icon: Int, text: Int, modifier: Modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
            Button(
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(backgroundColor = background),
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth(),
                onClick = {
                }) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = stringResource(id = text),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(dimensionResource(R.dimen.half_margin)))
            Text(stringResource(id = text),
                style = NaturblickTheme.typography.caption,
                color = NaturblickTheme.colors.onPrimaryHighEmphasis,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun homeButton(background: Color, icon: Int, text: Int, modifier: Modifier) {
        Button(
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = background),
            modifier = modifier.aspectRatio(1f),
            onClick = {
            }) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = stringResource(id = text),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    private fun createMenuItemListener(
        drawer: DrawerLayout
    ): (MenuItem) -> Boolean = { it: MenuItem ->
        val intent = when (it.itemId) {
            R.id.nav_start -> Intent(this, MainActivity::class.java)
            R.id.nav_field_book -> Intent(this, FieldbookActivity::class.java)
            R.id.nav_record_a_bird -> Intent(this, FieldbookActivity::class.java).apply {
                putExtra(ManageObservation.OBSERVATION_ACTION, CreateAudioObservation)
            }
            R.id.nav_photograph_a_plant -> Intent(this, FieldbookActivity::class.java).apply {
                putExtra(ManageObservation.OBSERVATION_ACTION, CreateImageObservation)
            }
            R.id.nav_account -> Intent(this, AccountActivity::class.java).apply {
                putExtra(CLOSE_ON_FINISHED, false)
            }
            R.id.nav_settings -> Intent(this, SettingsActivity::class.java)
            R.id.nav_feedback -> Intent(this, FeedbackActivity::class.java)
            R.id.nav_imprint -> Intent(this, ImprintActivity::class.java)
            R.id.nav_privacy -> Intent(this, GeneralPrivacyNoticeActivity::class.java)
            R.id.nav_about -> Intent(this, AboutActivity::class.java)
            R.id.nav_help -> Intent(this, HelpActivity::class.java)
            R.id.nav_accessibility -> Intent(this, AccessibilityActivity::class.java)
            else -> {
                throw IllegalStateException("Unknown navigation ID ${it.itemId}")
            }
        }
        intent.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)

        drawer.closeDrawer(GravityCompat.START)
        true
    }
}
