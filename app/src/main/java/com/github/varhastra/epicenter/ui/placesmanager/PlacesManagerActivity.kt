package com.github.varhastra.epicenter.ui.placesmanager

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.PlacesRepository
import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.ui.placeeditor.PlaceEditorActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_places_manager.*
import org.jetbrains.anko.design.snackbar
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

class PlacesManagerActivity : AppCompatActivity(), PlacesManagerContract.View {

    private lateinit var presenter: PlacesManagerContract.Presenter
    private lateinit var adapter: PlacesAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_manager)

        // Set up toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up recycler view
        adapter = PlacesAdapter(this, Prefs.getPreferredUnits())
        adapter.setHasStableIds(true)
        adapter.onStartDrag = this::onStartDrag
        adapter.onItemClick = { presenter.openEditor(it.id) }
        adapter.onDeleteItemClick = { presenter.tryDeletePlace(it) }
        adapter.onItemMoved = { presenter.saveOrder(adapter.data) }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = this.adapter
        recyclerView.setHasFixedSize(true)

        val dragHelperCallback = DragHelperCallback()
        dragHelperCallback.onMove = adapter::onItemMove
        dragHelperCallback.onPrepareItemMove = adapter::onPrepareItemMove

        itemTouchHelper = ItemTouchHelper(dragHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        addFab.setOnClickListener { presenter.openEditor(null) }

        PlacesManagerPresenter(this, PlacesRepository.getInstance())
    }

    override fun attachPresenter(presenter: PlacesManagerContract.Presenter) {
        this.presenter = presenter
    }

    override fun onResume() {
        super.onResume()

        presenter.start()
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

    override fun onPause() {
        presenter.deletePlace()
        presenter.saveOrder(adapter.data)

        super.onPause()
    }

    override fun isActive() = !isFinishing && !isDestroyed

    override fun showPlaces(places: List<Place>) {
        adapter.data = places.toMutableList()
    }

    override fun showEditor(placeId: Int?) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        if (placeId != null) {
            startActivity(intentFor<PlaceEditorActivity>(PlaceEditorActivity.EXTRA_PLACE_ID to placeId), options)
        } else {
            startActivity(intentFor<PlaceEditorActivity>(), options)
        }
    }

    override fun showUndoDeleteOption() {
        recyclerView.snackbar(R.string.places_manager_place_deleted, R.string.app_undo) {
            presenter.undoDeletion()
        }.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_SWIPE || event == BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_TIMEOUT) {
                    presenter.deletePlace()
                }
            }
        })
    }

    private fun onStartDrag(holder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(holder)
    }
}
