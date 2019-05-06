package com.github.varhastra.epicenter.ui.placesmanager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.PlacesRepository
import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.model.Place
import kotlinx.android.synthetic.main.activity_places_manager.*

class PlacesManagerActivity : AppCompatActivity(), PlacesManagerContract.View {

    private lateinit var presenter: PlacesManagerContract.Presenter
    private lateinit var adapter: PlacesAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places_manager)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        adapter = PlacesAdapter(this, Prefs.getPreferredUnits())
        adapter.onStartDrag = this::onStartDrag

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = this.adapter
        recyclerView.setHasFixedSize(true)

        val dragHelperCallback = DragHelperCallback()
        dragHelperCallback.onMove = adapter::onItemMove
        dragHelperCallback.onPrepareItemMove = adapter::onPrepareItemMove

        itemTouchHelper = ItemTouchHelper(dragHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        PlacesManagerPresenter(this, PlacesRepository.getInstance())
    }

    override fun attachPresenter(presenter: PlacesManagerContract.Presenter) {
        this.presenter = presenter
    }

    override fun onResume() {
        super.onResume()

        presenter.start()
    }

    override fun onPause() {
        presenter.saveOrder(adapter.data)

        super.onPause()
    }

    override fun isActive() = !isFinishing && !isDestroyed

    override fun showPlaces(places: List<Place>) {
        adapter.data = places.toMutableList()
    }

    override fun showEditor(placeId: Int) {
        // TODO
    }

    private fun onStartDrag(holder: RecyclerView.ViewHolder) {
        itemTouchHelper.startDrag(holder)
    }
}
