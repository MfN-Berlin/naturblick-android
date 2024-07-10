package berlin.mfn.naturblick.ui.species.specieslist

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.activity.viewModels
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.BaseActivity
import berlin.mfn.naturblick.ui.species.CharacterQuery
import berlin.mfn.naturblick.utils.AnalyticsTracker

class SpeciesListActivity : BaseActivity(R.navigation.species_list_navigation) {

    private lateinit var speciesListViewModel: SpeciesListViewModel
    private var isOpen: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeNavigationViews()

        val speciesDao = StrapiDb.getDb(applicationContext).speciesDao()

        val viewModel: SpeciesListViewModel by viewModels {
            SpeciesListViewModelFactory(speciesDao)
        }
        speciesListViewModel = viewModel

        val group = intent?.extras?.getString(QUERY_GROUP)
        group?.let {
            AnalyticsTracker.trackSpeciesGroup(
                application as NaturblickApplication,
                it
            )
        }

        speciesListViewModel.setGroup(group)
        val characters = intent?.extras?.getParcelable<CharacterQuery>(QUERY_CHARACTERS)
        speciesListViewModel.setCharacters(characters)
        characters?.let {
            AnalyticsTracker.trackMKey(
                application as NaturblickApplication,
                characters
            )
        }
        val resultName = intent.extras?.getString(QUERY_TITLE)
        resultName?.also {
            supportActionBar?.title = it
        } ?: run {
            supportActionBar?.setTitle(R.string.species)
        }

        handleIntent(intent)
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        handleIntent(intent)
        setIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val query = intent?.extras?.getString(SearchManager.QUERY)
        speciesListViewModel.setQuery(query)

        intent?.extras?.getBoolean(QUERY_IS_OPEN, true)?.let {
            isOpen = it
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        val inflater = menuInflater
        inflater.inflate(R.menu.species, menu)

        // Get the SearchView and set the searchable configuration
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.action_search).actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnCloseListener { ->
                speciesListViewModel.resetQuery()
                false
            }
            setOnQueryTextListener(object : OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    speciesListViewModel.setQuery(query)
                    return true
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    speciesListViewModel.setQuery(query)
                    return true
                }
            })

            // opens keyboard
            if (isOpen) {
                isIconifiedByDefault = false
                requestFocus()
            } else {
                isIconifiedByDefault = true
            }
        }

        return true
    }

    companion object {
        const val QUERY_GROUP = "group"
        const val QUERY_CHARACTERS = "characters"
        const val QUERY_TITLE = "title"
        const val QUERY_IS_OPEN = "is_open"
    }
}
