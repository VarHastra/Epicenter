package com.github.varhastra.epicenter.presentation.main.map


import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.children
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.presentation.details.DetailsActivity
import com.github.varhastra.epicenter.presentation.main.ToolbarProvider
import com.github.varhastra.epicenter.presentation.main.map.maputils.EventClusterItem
import com.github.varhastra.epicenter.presentation.main.map.maputils.EventsRenderer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.maps.android.clustering.ClusterManager
import org.jetbrains.anko.*

/**
 * A [Fragment] subclass that displays a map
 * of recent earthquakes.
 */
class MapFragment : BaseGmapsFragment(), OnMapReadyCallback, MapContract.View {

    private val logger = AnkoLogger(this.javaClass)

    @BindView(R.id.sheet_map)
    lateinit var filtersBottomSheet: ViewGroup

    @BindView(R.id.cg_map_filters_magnitude)
    lateinit var magnitudeChipGroup: ChipGroup

    @BindView(R.id.sb_map_filters_date)
    lateinit var dateSeekBar: SeekBar

    @BindView(R.id.pb_map)
    lateinit var progressBar: ContentLoadingProgressBar

    lateinit var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>

    var map: GoogleMap? = null
    lateinit var clusterManager: ClusterManager<EventClusterItem>

    lateinit var presenter: MapContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        ButterKnife.bind(this, view)

        setHasOptionsMenu(true)

        val mapView = view.findViewById<MapView>(R.id.map_view)
        onCreatingMapView(mapView, savedInstanceState)
        mapView.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(filtersBottomSheet)
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.start()

        magnitudeChipGroup.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach {
                (it as? Chip)?.apply {
                    isClickable = !isChecked
                }
            }

            val minMag = when (checkedId) {
                R.id.chip_mag_0 -> 0
                R.id.chip_mag_2 -> 2
                R.id.chip_mag_4 -> 4
                R.id.chip_mag_6 -> 6
                R.id.chip_mag_8 -> 8
                else -> 0
            }
            presenter.setMinMagnitude(minMag)
        }

        dateSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Do nothing
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                presenter.setPeriod(seekBar.progress + 1)
            }
        })
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
        map?.apply {
            val coordinates = Coordinates(cameraPosition.target.latitude, cameraPosition.target.longitude)
            presenter.saveCameraPosition(coordinates, cameraPosition.zoom)
        }
        super.onPause()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings.isMapToolbarEnabled = false
        try {
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            activity, R.raw.map_style
                    )
            )

            if (!success) {
                error("Error parsing map styles.")
            }
        } catch (e: Resources.NotFoundException) {
            error("Map style resource not found. ${e.stackTrace}.")
        }

        clusterManager = ClusterManager(activity, googleMap)
        clusterManager.renderer = EventsRenderer(activity!!, googleMap, clusterManager)
        clusterManager.setOnClusterItemInfoWindowClickListener { onMarkerInfoWindowClick(it) }
        googleMap.setOnCameraIdleListener(clusterManager)
        googleMap.setOnMarkerClickListener(clusterManager)
        googleMap.setOnInfoWindowClickListener(clusterManager)

        this.map = googleMap
        presenter.viewReady()
    }

    override fun setCameraPosition(coordinates: Coordinates, zoom: Float) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(coordinates.latitude, coordinates.longitude), zoom))
    }

    override fun attachPresenter(presenter: MapContract.Presenter) {
        this.presenter = presenter
    }

    override fun isActive() = isAdded

    override fun isReady() = map != null

    override fun showTitle() {
        activity?.apply {
            this as ToolbarProvider
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

    override fun showCurrentMagnitudeFilter(magnitude: Int) {
        val id = when (magnitude) {
            in -2 until 2 -> R.id.chip_mag_0
            in 2 until 4 -> R.id.chip_mag_2
            in 4 until 6 -> R.id.chip_mag_4
            in 6 until 8 -> R.id.chip_mag_6
            in 8..10 -> R.id.chip_mag_8
            else -> R.id.chip_mag_0
        }
        magnitudeChipGroup.check(id)
    }

    override fun showCurrentDaysFilter(days: Int) {
        dateSeekBar.progress = days - 1
    }

    override fun showEventMarkers(markers: List<EventMarker>) {
        map?.apply {
            doAsync {
                val clusterItems = markers.map { EventClusterItem.from(it) }
                uiThread {
                    logger.info("uiThread ${clusterItems.size}")
                    clusterManager.clearItems()
                    clusterManager.addItems(clusterItems)
                    clusterManager.cluster()
                }
            }
        }
    }

    override fun showEventDetails(eventId: String) {
        val host = activity
        host?.let {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(it).toBundle()
            startActivity(activity?.intentFor<DetailsActivity>(DetailsActivity.EXTRA_EVENT_ID to eventId), options)
        }
    }

    private fun onMarkerInfoWindowClick(eventsClusterItem: EventClusterItem) {
        presenter.openEventDetails(eventsClusterItem.eventId)
    }
}
