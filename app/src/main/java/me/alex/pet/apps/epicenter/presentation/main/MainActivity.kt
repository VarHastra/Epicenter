package me.alex.pet.apps.epicenter.presentation.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.presentation.main.feed.FeedFragment
import me.alex.pet.apps.epicenter.presentation.main.map.MapFragment
import me.alex.pet.apps.epicenter.presentation.settings.SettingsActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber


class MainActivity : AppCompatActivity(), ToolbarProvider {

    private val model: MainModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        observeModel()
    }

    private fun observeModel() = with(model) {
        bottomNavigationView.selectedItemId = selectedDestination.value!!.id
        selectedDestination.observe(this@MainActivity) { navigateTo(it) }
    }

    override fun onStart() {
        super.onStart()
        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavListener())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                SettingsActivity.start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // TODO: find a better place to check for play api availability
    private fun checkPlayApiAvailability() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionResult = apiAvailability.isGooglePlayServicesAvailable(this)

        if (connectionResult != ConnectionResult.SUCCESS) {
            Timber.e("GooglePlayServices are not available: $connectionResult ${apiAvailability.getErrorString(connectionResult)}")
            apiAvailability.getErrorDialog(this, connectionResult, 0).apply {
                setOnDismissListener {
                    finish()
                }
                show()
            }
        }
    }

    private fun navigateTo(destination: TabDestination) {
        val destinationFragment = when (destination) {
            TabDestination.FEED -> FeedFragment.newInstance(this@MainActivity)
            TabDestination.MAP -> MapFragment.newInstance(this@MainActivity)
        }
        replaceTabFragment(destinationFragment, destination.tag)
    }

    private fun replaceTabFragment(fragment: Fragment, tag: String) {
        if (supportFragmentManager.findFragmentByTag(tag) == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.frame_content_main, fragment, tag)
                    .commit()
        }
    }

    inner class BottomNavListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            model.onChangeDestination(item.itemId)
            return true
        }
    }


    override fun setTitleText(text: String) {
        toolbar.title = text
    }
}
