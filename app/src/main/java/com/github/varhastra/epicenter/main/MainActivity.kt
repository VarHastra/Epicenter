package com.github.varhastra.epicenter.main

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.main.feed.FeedFragment
import com.github.varhastra.epicenter.main.map.MapFragment
import com.github.varhastra.epicenter.main.notifications.NotificationsFragment
import com.github.varhastra.epicenter.main.search.SearchFragment
import com.github.varhastra.epicenter.views.ToolbarDropdown
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.jetbrains.anko.AnkoLogger

/**
 * Primary activity of the app that holds
 * the bottom navigation and hosts [FeedFragment],
 * [MapFragment], [NotificationsFragment] and [SearchFragment].
 */
class MainActivity : AppCompatActivity(), AnkoLogger {

    @BindView(R.id.tb_main)
    lateinit var toolbar: ToolbarDropdown

    @BindView(R.id.bnv_main)
    lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)

        bottomNavigation.setOnNavigationItemSelectedListener(BottomNavListener())
        if (savedInstanceState != null) {
            savedInstanceState.apply {
                bottomNavigation.selectedItemId = getInt(STATE_SELECTED_PAGE, R.id.navigation_feed)
            }
        } else {
            bottomNavigation.selectedItemId = R.id.navigation_feed
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.apply {
            putInt(STATE_SELECTED_PAGE, bottomNavigation.selectedItemId)
        }
    }

    private fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_content_main, fragment)
                .commit()
    }

    inner class BottomNavListener : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.navigation_feed -> {
                    navigateTo(FeedFragment())
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

    companion object {
        private const val STATE_SELECTED_PAGE = "STATE_SEL_PAGE"
    }
}
