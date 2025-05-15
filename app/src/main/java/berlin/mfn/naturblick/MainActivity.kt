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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import berlin.mfn.naturblick.databinding.ActivityMainBinding
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
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        setSupportActionBar(root.findViewById<View>(R.id.app_bar).findViewById(R.id.toolbar))

        val drawerLayout: DrawerLayout = root.findViewById(R.id.drawer_layout)
        navView = root.findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.start_navigation)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_start
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val menuItemSelectedListener = createMenuItemListener(
            drawerLayout
        )
        navView.setNavigationItemSelectedListener {
            menuItemSelectedListener(it)
        }
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
        supportActionBar?.title = null
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
