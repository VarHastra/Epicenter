package com.github.varhastra.epicenter.presentation.main.map


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.app.ActivityOptionsCompat
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.onStopTrackingTouch
import com.github.varhastra.epicenter.common.extensions.setRestrictiveCheckListener
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.presentation.common.EventMarker
import com.github.varhastra.epicenter.presentation.details.DetailsActivity
import com.github.varhastra.epicenter.presentation.main.ToolbarProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.sheet_map.*

class MapFragment : BaseMapFragment(), OnMapReadyCallback, MapContract.View {

    private lateinit var presenter: MapContract.Presenter

    private lateinit var map: GoogleMap

    private lateinit var clusterManager: ClusterManager<EventMarker>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior = BottomSheetBehavior.from(filtersSheet)
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        magnitudeChipGroup.setRestrictiveCheckListener { group, checkedId ->
            val minMag = when (checkedId) {
                R.id.magnitudeZeroChip -> MagnitudeLevel.ZERO_OR_LESS
                R.id.magnitudeTwoChip -> MagnitudeLevel.TWO
                R.id.magnitudeFourChip -> MagnitudeLevel.FOUR
                R.id.magnitudeSixChip -> MagnitudeLevel.SIX
                R.id.magnitudeEightChip -> MagnitudeLevel.EIGHT
                else -> MagnitudeLevel.ZERO_OR_LESS
            }
            presenter.setMinMagnitude(minMag)
        }

        numOfDaysSeekBar.onStopTrackingTouch { seekBar ->
            presenter.setNumberOfDaysToShow(seekBar.progress + 1)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_HIDDEN
                    else -> BottomSheetBehavior.STATE_EXPANDED
                }
                true
            }
            R.id.action_refresh -> {
                presenter.reloadEvents()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        val cameraPosition = map.cameraPosition
        val cameraTarget = cameraPosition.target
        val coordinates = Coordinates(cameraTarget.latitude, cameraTarget.longitude)
        presenter.saveCameraPosition(coordinates, cameraPosition.zoom)
        super.onPause()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        clusterManager = ClusterManager<EventMarker>(requireContext(), googleMap).apply {
            renderer = EventsRenderer(requireContext(), googleMap, this)
            setOnClusterItemInfoWindowClickListener { onMarkerInfoWindowClick(it) }
            setOnClusterClickListener { cluster ->
                val position = cluster.position
                presenter.onZoomIn(position.latitude, position.longitude)
                true
            }
        }

        this.map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            activity, R.raw.map_style
                    )
            )
            setOnCameraIdleListener(clusterManager)
            setOnMarkerClickListener(clusterManager)
            setOnInfoWindowClickListener(clusterManager)
        }

        presenter.start()
    }

    override fun setCameraPosition(coordinates: Coordinates, zoom: Float) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(coordinates.latitude, coordinates.longitude), zoom))
    }

    override fun attachPresenter(presenter: MapContract.Presenter) {
        this.presenter = presenter
    }

    override fun isActive() = isAdded

    override fun showTitle() {
        (requireActivity() as ToolbarProvider).run {
            showDropdown(false)
            setTitleText(getString(R.string.app_map))
        }
    }

    override fun showProgress(show: Boolean) {
        if (show) progressBar.show() else progressBar.hide()
    }

    override fun showFilters() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun showCurrentMagnitudeFilter(magnitudeLevel: MagnitudeLevel) {
        val id = when (magnitudeLevel) {
            MagnitudeLevel.ZERO_OR_LESS -> R.id.magnitudeZeroChip
            MagnitudeLevel.TWO -> R.id.magnitudeTwoChip
            MagnitudeLevel.FOUR -> R.id.magnitudeFourChip
            MagnitudeLevel.SIX -> R.id.magnitudeSixChip
            MagnitudeLevel.EIGHT -> R.id.magnitudeEightChip
            else -> R.id.magnitudeZeroChip
        }
        magnitudeChipGroup.check(id)
    }

    override fun showCurrentDaysFilter(days: Int) {
        numOfDaysSeekBar.progress = days - 1
    }

    override fun showEventMarkers(markers: List<EventMarker>) {
        clusterManager.clearItems()
        clusterManager.addItems(markers)
        clusterManager.cluster()
    }

    override fun showEventDetails(eventId: String) {
        val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EXTRA_EVENT_ID, eventId)
        }
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()
        startActivity(intent, options)
    }

    override fun zoomIn(latitude: Double, longitude: Double) {
        val position = LatLng(latitude, longitude)
        val zoom = map.cameraPosition.zoom + 2
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))
    }

    private fun onMarkerInfoWindowClick(eventMarker: EventMarker) {
        presenter.openEventDetails(eventMarker.eventId)
    }
}
