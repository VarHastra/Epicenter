package me.alex.pet.apps.epicenter.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.presentation.common.navigation.BackButtonListener
import me.alex.pet.apps.epicenter.presentation.common.navigation.Destination
import me.alex.pet.apps.epicenter.presentation.common.navigation.NavigationCommand
import me.alex.pet.apps.epicenter.presentation.common.navigation.Navigator

class HostActivity : AppCompatActivity(), Navigator {

//    private val router: Router by inject()

    private lateinit var transitionInflater: TransitionInflater

    private lateinit var enterTransition: Transition

    private lateinit var returnTransition: Transition


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)

        if (savedInstanceState == null) {
            val rootDestination = Destinations.Main()
            replaceFragment(rootDestination.newFragment(), rootDestination.tag, false)
        }
        preloadFragmentTransitions()
    }

    override fun onStart() {
        super.onStart()
//        router.attachNavigator(this)
    }

    override fun onStop() {
//        router.detachNavigator()
        super.onStop()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.hostContainer)
        if (fragment != null && fragment is BackButtonListener && fragment.onBackPressed()) {
            return
        } else {
            super.onBackPressed()
        }
    }

    override fun processNavCommand(command: NavigationCommand) {
        when (command) {
            is NavigationCommand.To -> navigateTo(command.destination)
            NavigationCommand.Back -> navigateBack()
            NavigationCommand.FinishFlow -> finishFlow()
        }
    }

    private fun navigateTo(destination: Destination) {
        val fragment = destination.newFragment()
        if (destination !is Destinations.Main) {
            applyTransitionTo(fragment)
        }
        replaceFragment(fragment, destination.tag, true)
    }

    private fun navigateBack() {
        val stackEntryCount = supportFragmentManager.backStackEntryCount
        if (stackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finishFlow()
        }
    }

    private fun finishFlow() {
        finish()
    }

    private fun applyTransitionTo(fragment: Fragment) {
        fragment.let {
            it.enterTransition = this.enterTransition
            it.returnTransition = this.returnTransition
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
