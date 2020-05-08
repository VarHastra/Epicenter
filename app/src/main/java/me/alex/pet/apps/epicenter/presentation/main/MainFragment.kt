package me.alex.pet.apps.epicenter.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_main.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.presentation.main.feed.FeedFragment
import me.alex.pet.apps.epicenter.presentation.main.map.MapFragment


class MainFragment : Fragment() {

    private val bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val tab = Tab.fromTabMenuId(item.itemId)
        replaceTabFragment(tab.newFragment())
        true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (childFragmentManager.findFragmentById(R.id.frame_content_main) == null) {
            val startTab = Tab.Feed
            bottomNavigationView.selectedItemId = startTab.tabMenuId
            replaceTabFragment(startTab.newFragment())
        }
    }

    override fun onStart() {
        super.onStart()
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavListener)
    }

    private fun replaceTabFragment(fragment: Fragment, tag: String = fragment::class.java.simpleName) {
        if (childFragmentManager.findFragmentByTag(tag) == null) {
            applyTransitionsTo(fragment)
            childFragmentManager.beginTransaction()
                    .replace(R.id.frame_content_main, fragment, tag)
                    .commit()
        }
    }

    private fun applyTransitionsTo(fragment: Fragment) {
        val transitionInflater = TransitionInflater.from(context)
        val enterAnim = transitionInflater.inflateTransition(R.transition.transition_main_enter)
        val exitAnim = transitionInflater.inflateTransition(R.transition.transition_main_exit)
        fragment.apply {
            enterTransition = enterAnim
            exitTransition = exitAnim
        }
    }


    companion object {
        fun newInstance() = MainFragment()
    }


    private sealed class Tab(val id: Int, @IdRes val tabMenuId: Int) {

        abstract fun newFragment(): Fragment

        object Feed : Tab(0, R.id.navigation_feed) {
            override fun newFragment(): Fragment = FeedFragment.newInstance()
        }

        object Map : Tab(1, R.id.navigation_map) {
            override fun newFragment(): Fragment = MapFragment.newInstance()
        }

        companion object {
            fun fromId(id: Int): Tab {
                return when (id) {
                    Feed.id -> Feed
                    Map.id -> Map
                    else -> throw IllegalArgumentException("Unknown tab id: $id.")
                }
            }

            fun fromTabMenuId(tabMenuId: Int): Tab {
                return when (tabMenuId) {
                    Feed.tabMenuId -> Feed
                    Map.tabMenuId -> Map
                    else -> throw IllegalArgumentException("Unknown tabMenuId id: $tabMenuId.")
                }
            }
        }
    }
}
