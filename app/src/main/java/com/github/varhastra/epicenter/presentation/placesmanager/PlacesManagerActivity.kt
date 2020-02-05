package com.github.varhastra.epicenter.presentation.placesmanager

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.snackbar
import com.github.varhastra.epicenter.data.AppSettings
import com.github.varhastra.epicenter.data.PlacesDataSource
import com.github.varhastra.epicenter.domain.interactors.DeletePlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlacesInteractor
import com.github.varhastra.epicenter.domain.interactors.UpdatePlacesOrderInteractor
import com.github.varhastra.epicenter.presentation.placeeditor.PlaceEditorActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_places_manager.*

class PlacesManagerActivity : AppCompatActivity(), PlacesManagerContract.View {

    private lateinit var presenter: PlacesManagerContract.Presenter

    private lateinit var placesAdapter: PlacesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_manager)

        setUpViews()

        val placesRepository = PlacesDataSource.getInstance()
        PlacesManagerPresenter(
                this,
                this,
                LoadPlacesInteractor(placesRepository),
                DeletePlaceInteractor(placesRepository),
                UpdatePlacesOrderInteractor(placesRepository),
                AppSettings
        )
    }

    private fun setUpViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        placesAdapter = PlacesAdapter(this).apply {
            setHasStableIds(true)
            onItemClick = { presenter.editPlace(it) }
            onDeleteItem = { presenter.tryDeletePlace(it) }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlacesManagerActivity)
            adapter = placesAdapter
            setHasFixedSize(true)
        }

        addFab.setOnClickListener { presenter.addPlace() }
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
        presenter.apply {
            deletePlace()
            saveOrder(placesAdapter.data)
        }

        super.onPause()
    }

    override fun isActive() = !isFinishing && !isDestroyed

    override fun showPlaces(places: List<PlaceViewBlock>) {
        placesAdapter.data = places.toMutableList()
    }

    override fun showPlaceEditor(placeId: Int) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        val intent = Intent(this, PlaceEditorActivity::class.java).apply {
            putExtra(PlaceEditorActivity.EXTRA_PLACE_ID, placeId)
        }
        startActivity(intent, options)
    }

    override fun showPlaceCreator() {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        val intent = Intent(this, PlaceEditorActivity::class.java)
        startActivity(intent, options)
    }

    override fun showUndoDeleteOption() {
        val snackbar = recyclerView.snackbar(R.string.places_manager_place_deleted, R.string.app_undo) {
            presenter.undoDeletion()
        }
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar, event: Int) {
                if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_TIMEOUT) {
                    presenter.deletePlace()
                }
            }
        })
    }
}
