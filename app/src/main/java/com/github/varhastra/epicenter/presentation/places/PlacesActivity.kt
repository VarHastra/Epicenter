package com.github.varhastra.epicenter.presentation.places

import android.app.Activity
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
import kotlinx.android.synthetic.main.activity_places.*

class PlacesActivity : AppCompatActivity(), PlacesContract.View {

    private lateinit var presenter: PlacesContract.Presenter

    private lateinit var placesAdapter: PlacesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        setUpViews()

        val placesRepository = PlacesDataSource.getInstance()
        PlacesPresenter(
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
            layoutManager = LinearLayoutManager(this@PlacesActivity)
            adapter = placesAdapter
            setHasFixedSize(true)
        }

        addFab.setOnClickListener { presenter.addPlace() }
    }

    override fun attachPresenter(presenter: PlacesContract.Presenter) {
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
        PlaceEditorActivity.start(this, placeId)
    }

    override fun showPlaceCreator() {
        PlaceEditorActivity.start(this)
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


    companion object {

        fun start(sourceActivity: Activity) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(sourceActivity).toBundle()
            val intent = Intent(sourceActivity, PlacesActivity::class.java)
            sourceActivity.startActivity(intent, options)
        }
    }
}
