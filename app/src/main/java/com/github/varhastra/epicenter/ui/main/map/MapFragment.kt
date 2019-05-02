package com.github.varhastra.epicenter.ui.main.map


import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.ui.details.DetailsActivity
import com.github.varhastra.epicenter.ui.main.ToolbarProvider
import com.github.varhastra.epicenter.ui.main.map.maputils.EventClusterItem
import com.github.varhastra.epicenter.ui.main.map.maputils.EventsRenderer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

    lateinit var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>

    var map: GoogleMap? = null
    lateinit var clusterManager: ClusterManager<EventClusterItem>

    lateinit var presenter: MapContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        ButterKnife.bind(this, view)

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
        startActivity(activity?.intentFor<DetailsActivity>(DetailsActivity.EXTRA_EVENT_ID to eventId))
    }

    private fun onMarkerInfoWindowClick(eventsClusterItem: EventClusterItem) {
        presenter.openEventDetails(eventsClusterItem.eventId)
    }
}
