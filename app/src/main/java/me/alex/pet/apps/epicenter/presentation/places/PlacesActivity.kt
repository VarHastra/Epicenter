package me.alex.pet.apps.epicenter.presentation.places

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_places.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.presentation.placeeditor.PlaceEditorActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class PlacesActivity : AppCompatActivity() {

    private val model: PlacesModel by viewModel()

    private lateinit var placesAdapter: PlacesAdapter

    private lateinit var undoDeletionSnackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        setUpViews()
        observeModel()
    }

    private fun setUpViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        placesAdapter = PlacesAdapter(this).apply {
            setHasStableIds(true)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlacesActivity)
            adapter = placesAdapter
            setHasFixedSize(true)
        }

        undoDeletionSnackbar = Snackbar.make(rootCoordinatorLayout, "", Snackbar.LENGTH_LONG)
    }

    private fun observeModel() = with(model) {
        places.observe(this@PlacesActivity, ::renderPlaces)

        addNewPlaceEvent.observe(this@PlacesActivity) { event ->
            Timber.d("addNewPlaceEvent")
            event.consume { renderPlaceCreator() }
        }
        editPlaceEvent.observe(this@PlacesActivity) { event ->
            event.consume { placeId -> renderPlaceEditor(placeId) }
        }
        deletionAttemptEvent.observe(this@PlacesActivity) { event ->
            event.consume { numberOfDeletedItems -> showUndoDeleteOption(numberOfDeletedItems) }
        }
    }

    override fun onStart() {
        super.onStart()
        model.onStart()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        model.apply {
            onDeletePlaces()
            onSaveOrder(placesAdapter.data)
        }
        super.onBackPressed()
    }

    private fun renderPlaces(places: List<PlaceViewBlock>) {
        placesAdapter.data = places.toMutableList()
    }

    private fun renderPlaceEditor(placeId: Int) {
        PlaceEditorActivity.start(this, placeId)
    }

    private fun renderPlaceCreator() {
        PlaceEditorActivity.start(this)
    }

    private fun showUndoDeleteOption(numberOfDeletedItems: Int) {
        val notice = resources.getQuantityString(R.plurals.plurals_places_deleted, numberOfDeletedItems, numberOfDeletedItems)
        undoDeletionSnackbar.setText(notice)
        if (!undoDeletionSnackbar.isShown) {
            undoDeletionSnackbar.show()
        }
    }


    companion object {

        fun start(sourceActivity: Activity) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(sourceActivity).toBundle()
            val intent = Intent(sourceActivity, PlacesActivity::class.java)
            sourceActivity.startActivity(intent, options)
        }
    }
}
