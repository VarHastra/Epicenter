package me.alex.pet.apps.epicenter.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_main.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.presentation.main.feed.FeedFragment
import me.alex.pet.apps.epicenter.presentation.main.map.MapFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment : Fragment() {

    private val model: MainModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        model.onRestoreState(savedInstanceState)
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeModel()
    }

    private fun observeModel() = with(model) {
        bottomNavigationView.selectedItemId = selectedDestination.value!!.id
        selectedDestination.observe(viewLifecycleOwner) { navigateTo(it) }
    }

    override fun onStart() {
        super.onStart()
        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavListener())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        model.onSaveState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun navigateTo(destination: TabDestination) {
        val destinationFragment = when (destination) {
            TabDestination.FEED -> FeedFragment.newInstance(requireContext())
            TabDestination.MAP -> MapFragment.newInstance(requireContext())
        }
        replaceTabFragment(destinationFragment, destination.tag)
    }

    private fun replaceTabFragment(fragment: Fragment, tag: String) {
        if (childFragmentManager.findFragmentByTag(tag) == null) {
            childFragmentManager.beginTransaction()
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


    companion object {
        fun newInstance() = MainFragment()
    }
}
