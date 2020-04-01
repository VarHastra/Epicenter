package me.alex.pet.apps.epicenter.presentation.places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_places.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.presentation.locationpicker.LocationPickerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PlacesFragment : Fragment() {

    private val model: PlacesModel by viewModel()

    private lateinit var placesAdapter: PlacesAdapter

    private lateinit var undoDeletionSnackbar: Snackbar


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return layoutInflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews()
        observeModel()
    }

    private fun setUpViews() {
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_up)
            setTitle(R.string.places_title)
        }

        placesAdapter = PlacesAdapter(requireContext()).apply {
            setHasStableIds(true)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = placesAdapter
            setHasFixedSize(true)
        }

        undoDeletionSnackbar = Snackbar.make(rootCoordinatorLayout, "", Snackbar.LENGTH_LONG)
    }

    private fun observeModel() = with(model) {
        places.observe(viewLifecycleOwner, ::renderPlaces)

        addNewPlaceEvent.observe(viewLifecycleOwner) { event ->
            Timber.d("addNewPlaceEvent")
            event.consume { renderPlaceCreator() }
        }
        editPlaceEvent.observe(viewLifecycleOwner) { event ->
            event.consume { placeId -> renderPlaceEditor(placeId) }
        }
        deletionAttemptEvent.observe(viewLifecycleOwner) { event ->
            event.consume { numberOfDeletedItems -> showUndoDeleteOption(numberOfDeletedItems) }
        }
    }

    override fun onStart() {
        super.onStart()
        model.onStart()

        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }

        placesAdapter.apply {
            onItemClick = { model.onEditPlace(it) }
            onDeleteItem = { model.onAttemptToDeletePlace(it) }
        }

        addFab.setOnClickListener { model.onAddNewPlace() }

        undoDeletionSnackbar.apply {
            setAction(R.string.app_action_undo) {
                model.onUndoDeletion()
            }
            addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                    if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                        model.onDeletePlaces()
                    }
                }
            })
        }
    }

    override fun onStop() {
        model.apply {
            onDeletePlaces()
            onSaveOrder(placesAdapter.data)
        }
        super.onStop()
    }

    private fun renderPlaces(places: List<PlaceViewBlock>) {
        placesAdapter.data = places.toMutableList()
    }

    private fun renderPlaceEditor(placeId: Int) {
        LocationPickerActivity.start(requireActivity(), placeId)
    }

    private fun renderPlaceCreator() {
        LocationPickerActivity.start(requireActivity())
    }

    private fun showUndoDeleteOption(numberOfDeletedItems: Int) {
        val notice = resources.getQuantityString(R.plurals.plurals_places_deleted, numberOfDeletedItems, numberOfDeletedItems)
        undoDeletionSnackbar.setText(notice)
        if (!undoDeletionSnackbar.isShown) {
            undoDeletionSnackbar.show()
        }
    }


    companion object {

        fun newInstance(): Fragment = PlacesFragment()
    }
}
