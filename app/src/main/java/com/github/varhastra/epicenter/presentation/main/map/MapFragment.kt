package com.github.varhastra.epicenter.presentation.main.map


import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
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
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.sheet_map.*
import org.jetbrains.anko.*

/**
 * A [Fragment] subclass that displays a map
 * of recent earthquakes.
 */
class MapFragment : BaseGmapsFragment(), OnMapReadyCallback, MapContract.View {

    private val logger = AnkoLogger(this.javaClass)

    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    var map: GoogleMap? = null
    lateinit var clusterManager: ClusterManager<EventClusterItem>

    lateinit var presenter: MapContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        setHasOptionsMenu(true)

        val mapView = view.findViewById<MapView>(R.id.map)
        onCreatingMapView(mapView, savedInstanceState)
        mapView.getMapAsync(this)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior = BottomSheetBehavior.from(filtersSheet)
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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
                R.id.magnitudeZeroChip -> MagnitudeLevel.ZERO_OR_LESS
                R.id.magnitudeTwoChip -> MagnitudeLevel.TWO
                R.id.magnitudeFourChip -> MagnitudeLevel.FOUR
                R.id.magnitudeSixChip -> MagnitudeLevel.SIX
                R.id.magnitudeEightChip -> MagnitudeLevel.EIGHT
                else -> MagnitudeLevel.ZERO_OR_LESS
            }
            presenter.setMinMagnitude(minMag)
        }

        numOfDaysSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Do nothing
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                presenter.setNumberOfDaysToShow(seekBar.progress + 1)
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
