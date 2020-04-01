package me.alex.pet.apps.epicenter.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.presentation.common.navigation.Destination
import me.alex.pet.apps.epicenter.presentation.common.navigation.Destinations
import me.alex.pet.apps.epicenter.presentation.common.navigation.Navigator
import me.alex.pet.apps.epicenter.presentation.common.navigation.Router
import org.koin.android.ext.android.inject

class HostActivity : AppCompatActivity(), Navigator {

    private val router: Router by inject()

    private lateinit var transitionInflater: TransitionInflater

    private lateinit var enterTransition: Transition

    private lateinit var returnTransition: Transition


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        if (savedInstanceState == null) {
            val rootDestination = Destinations.Main()
            replaceFragment(rootDestination.fragment, rootDestination.tag, false)
        }
        preloadFragmentTransitions()
    }

    override fun onStart() {
        super.onStart()
        router.attachNavigator(this)
    }

    override fun onStop() {
        router.detachNavigator()
        super.onStop()
    }

    override fun navigateTo(destination: Destination) {
        applyTransitionTo(destination)
        replaceFragment(destination.fragment, destination.tag, true)
    }

    override fun navigateBack() {
        supportFragmentManager.popBackStack()
    }

    private fun applyTransitionTo(destination: Destination) {
        if (destination is Destinations.Main) {
            return
        }

        destination.fragment.let {
            it.enterTransition = enterTransition
            it.returnTransition = returnTransition
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
                .replace(R.id.hostContainer, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    private fun preloadFragmentTransitions() {
        transitionInflater = TransitionInflater.from(this@HostActivity)
        enterTransition = transitionInflater.inflateTransition(R.transition.transition_all_enter)
        returnTransition = transitionInflater.inflateTransition(R.transition.transition_all_return)
    }
}
