package me.alex.pet.apps.epicenter.presentation.main

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.data.AppState
import me.alex.pet.apps.epicenter.presentation.main.feed.FeedFragment
import me.alex.pet.apps.epicenter.presentation.main.map.MapFragment
import me.alex.pet.apps.epicenter.presentation.settings.SettingsActivity
import timber.log.Timber


class MainActivity : AppCompatActivity(), ToolbarProvider {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        checkPlayApiAvailability()

        if (AppState.isFirstLaunch) {
            checkLocationPermission()
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavListener())
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_feed
        }
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

    private fun checkPlayApiAvailability() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionResult = apiAvailability.isGooglePlayServicesAvailable(this)

        if (connectionResult == ConnectionResult.SUCCESS) {
            return
        }

        Timber.e("GooglePlayServices are not available: ${apiAvailability.getErrorString(connectionResult)}")

        if (apiAvailability.isUserResolvableError(connectionResult)) {
            apiAvailability.getErrorDialog(this, connectionResult, 0).apply {
                setOnDismissListener {
                    finish()
                }
                show()
            }
        } else {
            finish()
        }
    }

    private fun checkLocationPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        AppState.isFirstLaunch = false
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        AppState.isFirstLaunch = false
                    }
                }).check()
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_content_main, fragment)
                .commit()
    }

    inner class BottomNavListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.navigation_feed -> {
                    replaceFragment(FeedFragment.newInstance(this@MainActivity))
                    true
                }
                R.id.navigation_map -> {
                    replaceFragment(MapFragment.newInstance(this@MainActivity))
                    true
                }
                else -> false
            }
        }
    }


    override fun setTitleText(text: String) {
        toolbar.title = text
    }
}
