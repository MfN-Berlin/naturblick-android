/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.ActivityDefaultBinding
import com.google.android.material.navigation.NavigationView

data class Binding(val root: View, val navigationView: NavigationView)

open class BaseActivity(
    private val navigationGraph: Int
) : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    protected fun initializeNavigationViews(
        existingRoot: View? = null,
        alternativeStart: Int? = null,
        extras: Bundle? = null
    ) {
        val root = existingRoot ?: ActivityDefaultBinding.inflate(layoutInflater).root
        setContentView(root)
        setSupportActionBar(root.findViewById<View>(R.id.app_bar).findViewById(R.id.toolbar))

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(navigationGraph)
        alternativeStart?.let {
            navGraph.setStartDestination(it)
        }
        val bundle = if (extras != null) {
            Bundle().apply {
                putAll(intent.extras)
                putAll(extras)
            }
        } else {
            intent.extras
        }
        navController.setGraph(navGraph, bundle)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_start
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return if (onLeave { closeActivity ->
            if (closeActivity) {
                setResult(RESULT_CANCELED)
                finish()
            } else {
                val navController = findNavController(R.id.nav_host_fragment)
                navController.navigateUp(appBarConfiguration) ||
                    super.onSupportNavigateUp() ||
                    super.onBackPressed().let { true }
            }
        }
        ) {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigateUp(appBarConfiguration) ||
                super.onSupportNavigateUp() ||
                super.onBackPressed().let { true }
        } else {
            false
        }
    }

    override fun onBackPressed() {
        if (onLeave { closeActivity ->
            if (closeActivity) {
                finish()
            } else {
                super.onBackPressed()
            }
        }
        ) {
            super.onBackPressed()
        }
    }

    protected open fun onLeave(leave: (Boolean) -> Unit): Boolean = true
}
