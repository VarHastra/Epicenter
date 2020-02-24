package com.github.varhastra.epicenter.presentation.main

import android.Manifest
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.AppState
import com.github.varhastra.epicenter.presentation.main.feed.FeedFragment
import com.github.varhastra.epicenter.presentation.main.map.MapFragment
import com.github.varhastra.epicenter.presentation.settings.SettingsActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import org.jetbrains.anko.warn

/**
 * Primary activity of the app that holds
 * the bottom navigation and hosts [FeedFragment],
 * [MapFragment], [NotificationsFragment] and [SearchFragment].
 */
class MainActivity : AppCompatActivity(), AnkoLogger, ToolbarProvider {

    @BindView(R.id.tb_main)
    lateinit var toolbar: Toolbar

    @BindView(R.id.bnv_main)
    lateinit var bottomNavigation: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)

        info("onCreate")

        if (checkPlayApiAvailability()) {
            if (AppState.isFirstLaunch) {
                checkLocationPermission()
            }
        }

        bottomNavigation.setOnNavigationItemSelectedListener(BottomNavListener())
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.navigation_feed
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

    private fun checkPlayApiAvailability(): Boolean {
        val api = GoogleApiAvailability.getInstance()
        val availability = api.isGooglePlayServicesAvailable(this)
        when (availability) {
            ConnectionResult.SERVICE_MISSING,
            ConnectionResult.SERVICE_UPDATING,
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
            ConnectionResult.SERVICE_DISABLED,
            ConnectionResult.SERVICE_INVALID -> {
                if (api.isUserResolvableError(availability)) {
                    warn("GooglePlayServices are not available. Code: $availability")
                    val dialog = api.getErrorDialog(this, availability, 0)
                    dialog.setOnDismissListener {
                        finish()
                    }
                    dialog.show()
                } else {
                    error("Unresolvable issue with GooglePlayServices. Code: $availability")
                    finish()
                }
                return false
            }
        }

        return true
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
