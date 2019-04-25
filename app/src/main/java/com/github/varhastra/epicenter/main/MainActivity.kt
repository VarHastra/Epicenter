package com.github.varhastra.epicenter.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.EventsRepository
import com.github.varhastra.epicenter.data.PlacesRepository
import com.github.varhastra.epicenter.data.networking.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.main.feed.FeedFragment
import com.github.varhastra.epicenter.main.feed.FeedPresenter
import com.github.varhastra.epicenter.main.map.MapFragment
import com.github.varhastra.epicenter.main.notifications.NotificationsFragment
import com.github.varhastra.epicenter.main.search.SearchFragment
import com.github.varhastra.epicenter.settings.SettingsActivity
import com.github.varhastra.epicenter.views.ToolbarDropdown
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
import org.jetbrains.anko.displayMetrics
import org.jetbrains.anko.error
import org.jetbrains.anko.warn

/**
 * Primary activity of the app that holds
 * the bottom navigation and hosts [FeedFragment],
 * [MapFragment], [NotificationsFragment] and [SearchFragment].
 */
class MainActivity : AppCompatActivity(), AnkoLogger, ToolbarProvider {

    @BindView(R.id.tb_main)
    lateinit var toolbar: ToolbarDropdown

    @BindView(R.id.bnv_main)
    lateinit var bottomNavigation: BottomNavigationView

    var popupWindow: ListPopupWindow? = null

    // Adapter for ListPopupWindow that displays a list of Places for FeedFragment
    val placesAdapter = PlacesAdapter(this@MainActivity)

    var placesPopupListener: ((Place) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        toolbar.setOnDropdownClickListener { showDropdownPopup() }

        bottomNavigation.setOnNavigationItemSelectedListener(BottomNavListener())
        if (savedInstanceState != null) {
            savedInstanceState.apply {
                bottomNavigation.selectedItemId = getInt(STATE_SELECTED_PAGE, R.id.navigation_feed)
            }
        } else {
            bottomNavigation.selectedItemId = R.id.navigation_feed
        }


    }

    override fun onResume() {
        super.onResume()
        checkPlayApiAvailability()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.apply {
            putInt(STATE_SELECTED_PAGE, bottomNavigation.selectedItemId)
        }
    }

    override fun onStop() {
        popupWindow?.dismiss()
        popupWindow?.setAdapter(null)
        popupWindow?.anchorView = null
        popupWindow?.setOnItemClickListener(null)
        popupWindow = null
        super.onStop()
    }

    private fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_content_main, fragment)
                .commit()
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

    inner class BottomNavListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.navigation_feed -> {
                    val fragment = FeedFragment()
                    // TODO: replace mock provider with the real one
                    FeedPresenter(
                            fragment,
                            EventsRepository.getInstance(UsgsServiceProvider()),
                            PlacesRepository.getInstance(),
                            LocationProvider()
                    )
                    navigateTo(fragment)
                    true
                }
                R.id.navigation_map -> {
                    navigateTo(MapFragment())
                    true
                }
                R.id.navigation_search -> {
                    navigateTo(SearchFragment())
                    true
                }
                R.id.navigation_notifications -> {
                    navigateTo(NotificationsFragment())
                    true
                }
                else -> false
            }
        }
    }


    override fun setTitleText(text: String) = toolbar.setTitleText(text)

    override fun setDropdownText(text: String) = toolbar.setDropdownText(text)

    override fun showDropdown(show: Boolean) = toolbar.showDropDown(show)

    private fun showDropdownPopup() {
        popupWindow = ListPopupWindow(this).apply {
            anchorView = toolbar.dropdownTextView
            setBackgroundDrawable(getDrawable(R.drawable.bg_popup_window))
            width = displayMetrics.widthPixels / 2
            height = displayMetrics.heightPixels / 3
            setAdapter(placesAdapter)
            setOnItemClickListener { parent, _, position, _ ->
                placesPopupListener?.invoke(parent.adapter.getItem(position) as Place)
                this.dismiss()
            }
            setListSelector(getDrawable(R.drawable.bg_transparent))
            show()
        }
    }

    override fun attachListener(listener: (Place) -> Unit) {
        placesPopupListener = listener
    }

    override fun setDropdownData(places: List<Place>) {
        placesAdapter.places = places
        placesAdapter.notifyDataSetChanged()
    }


    companion object {
        private const val STATE_SELECTED_PAGE = "STATE_SEL_PAGE"
    }
}
