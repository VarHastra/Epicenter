package me.alex.pet.apps.epicenter.presentation.placeeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.presentation.common.navigation.BackButtonListener
import me.alex.pet.apps.epicenter.presentation.common.navigation.Destination
import me.alex.pet.apps.epicenter.presentation.common.navigation.NavigationCommand
import me.alex.pet.apps.epicenter.presentation.common.navigation.Navigator
import me.alex.pet.apps.epicenter.presentation.placeeditor.navigation.PlaceEditorDestinations
import me.alex.pet.apps.epicenter.presentation.placeeditor.navigation.PlaceEditorNavigator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class PlaceEditorFragment : Fragment(), PlaceEditorNavigator, BackButtonListener {

    private val model: PlaceEditorModel by viewModel {
        val arguments = arguments
        val placeId = if (arguments != null && arguments.containsKey(EXTRA_PLACE_ID)) {
            arguments.getInt(EXTRA_PLACE_ID)
        } else {
            null
        }
        parametersOf(placeId)
    }

    private lateinit var enterTransition: Transition

    private lateinit var returnTransition: Transition


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.onRestoreState(savedInstanceState)

        if (savedInstanceState == null) {
            val startDestination = PlaceEditorDestinations.LocationPicker()
            replaceFragment(startDestination.newFragment(), startDestination.tag, false)
        }
        preloadFragmentTransitions()
    }

    private fun preloadFragmentTransitions() {
        val transitionInflater = TransitionInflater.from(requireContext())
        enterTransition = transitionInflater.inflateTransition(R.transition.transition_all_enter)
        returnTransition = transitionInflater.inflateTransition(R.transition.transition_all_return)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_place_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.consume { command -> processNavCommand(command) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        model.onSaveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed(): Boolean {
        navigateBack()
        return true
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
        applyTransitionTo(fragment)
        replaceFragment(fragment, destination.tag, true)
    }

    private fun applyTransitionTo(fragment: Fragment) {
        fragment.let {
            it.enterTransition = this.enterTransition
            it.returnTransition = this.returnTransition
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        val transaction = childFragmentManager.beginTransaction()
                .replace(R.id.placeEditorContainer, fragment, tag)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    private fun navigateBack() {
        val stackEntryCount = childFragmentManager.backStackEntryCount
        if (stackEntryCount > 0) {
            childFragmentManager.popBackStack()
        } else {
            finishFlow()
        }
    }

    private fun finishFlow() {
        (requireActivity() as Navigator).processNavCommand(NavigationCommand.Back)
    }

    companion object {
        fun newInstance(placeId: Int?): Fragment {
            val fragment = PlaceEditorFragment()
            if (placeId != null) {
                fragment.arguments = Bundle().apply {
                    putInt(EXTRA_PLACE_ID, placeId)
                }
            }
            return fragment
        }
    }
}

private const val EXTRA_PLACE_ID = "EXTRA_PLACE_ID"
