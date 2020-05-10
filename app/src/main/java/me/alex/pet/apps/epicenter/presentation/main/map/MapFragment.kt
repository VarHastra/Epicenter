package me.alex.pet.apps.epicenter.presentation.main.map


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.sheet_map.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.common.extensions.onStopTrackingTouch
import me.alex.pet.apps.epicenter.common.extensions.setRestrictiveCheckListener
import me.alex.pet.apps.epicenter.domain.model.Coordinates
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.domain.state.CameraState
import me.alex.pet.apps.epicenter.presentation.common.EventMarker
import me.alex.pet.apps.epicenter.presentation.common.navigation.Navigator
import org.koin.androidx.viewmodel.ext.android.viewModel

class MapFragment : Fragment(), OnMapReadyCallback {

    private val model: MapModel by viewModel()

    private var map: GoogleMap? = null

    private lateinit var clusterManager: ClusterManager<EventMarker>

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(this)

        toolbar.inflateMenu(R.menu.menu_main)

        bottomSheetBehavior = BottomSheetBehavior.from(filtersSheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        toolbar.title = getString(R.string.app_map)
    }

    override fun onStart() {
        super.onStart()

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_filter -> {
                    model.onToggleFiltersVisibility()
                    true
                }
                R.id.action_refresh -> {
                    model.onRefreshEvents()
                    true
                }
                R.id.action_settings -> {
                    model.onOpenSettings()
                    true
                }
                else -> false
            }
        }

        magnitudeChipGroup.setRestrictiveCheckListener { _, checkedId ->
            val minMag = when (checkedId) {
                R.id.magnitudeZeroChip -> MagnitudeLevel.ZERO_OR_LESS
                R.id.magnitudeTwoChip -> MagnitudeLevel.TWO
                R.id.magnitudeFourChip -> MagnitudeLevel.FOUR
                R.id.magnitudeSixChip -> MagnitudeLevel.SIX
                R.id.magnitudeEightChip -> MagnitudeLevel.EIGHT
                else -> MagnitudeLevel.ZERO_OR_LESS
            }
            model.onChangeMinMagnitude(minMag)
        }

        numOfDaysSeekBar.onStopTrackingTouch { seekBar ->
            model.onChangeNumberOfDaysToShow(seekBar.progress + 1)
        }

        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Intentionally do nothing
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    filtersFab.show()
                }
            }
        })

        hideFiltersBtn.setOnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_DRAGGING) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        filtersFab.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            filtersFab.hide()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.clear()

        clusterManager = ClusterManager<EventMarker>(requireContext(), googleMap).apply {
            renderer = EventsRenderer(requireContext(), googleMap, this)
            setOnClusterItemInfoWindowClickListener { eventMarker ->
                model.onOpenDetails(eventMarker.eventId)
            }
            setOnClusterClickListener { cluster ->
                val position = cluster.position
                model.onZoomIn(position.latitude, position.longitude)
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

        observeModel()
    }

    private fun observeModel() = with(model) {
        eventMarkers.observe(viewLifecycleOwner, ::renderEventMarkers)
        isLoading.observe(viewLifecycleOwner, ::renderProgressBar)
        minMagnitude.observe(viewLifecycleOwner, ::renderMinMagnitude)
        numberOfDaysToShow.observe(viewLifecycleOwner, ::renderNumberOfDaysToShow)

        toggleFiltersEvent.observe(viewLifecycleOwner) { event ->
            event.consume { renderFilters() }
        }
        zoomInEvent.observe(viewLifecycleOwner) { event ->
            event.consume { coordinates -> zoomIn(coordinates.latitude, coordinates.longitude) }
        }
        updateCameraPositionEvent.observe(viewLifecycleOwner) { event ->
            event.consume { cameraState -> changeCameraPosition(cameraState) }
        }

        navigationEvent.observe(viewLifecycleOwner) { event ->
            event.consume { command ->
                (requireActivity() as Navigator).processNavCommand(command)
            }
        }
    }

    override fun onStop() {
        val map = this.map
        if (map != null) {
            val cameraTarget = map.cameraPosition.target
            val zoom = map.cameraPosition.zoom
            val coordinates = Coordinates(cameraTarget.latitude, cameraTarget.longitude)
            model.onRememberCameraPosition(coordinates, zoom)
        }
        super.onStop()
    }

    private fun renderEventMarkers(markers: List<EventMarker>) {
        clusterManager.run {
            clearItems()
            addItems(markers)
            cluster()
        }
    }

    private fun renderProgressBar(isLoading: Boolean) {
        if (isLoading) progressBar.show() else progressBar.hide()
    }

    private fun renderFilters() {
        bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_HIDDEN
            else -> BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun renderMinMagnitude(magnitudeLevel: MagnitudeLevel) {
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

    private fun renderNumberOfDaysToShow(days: Int) {
        numOfDaysSeekBar.progress = days - 1
    }

    private fun changeCameraPosition(cameraState: CameraState) {
        val (zoom, coordinates) = cameraState
        requireMap().moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(coordinates.latitude, coordinates.longitude), zoom))
    }

    private fun zoomIn(latitude: Double, longitude: Double) {
        val position = LatLng(latitude, longitude)
        val zoom = requireMap().cameraPosition.zoom + 2
        requireMap().animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom))
    }

    private fun requireMap(): GoogleMap {
        return this.map ?: throw NullPointerException()
    }


    companion object {
        fun newInstance(): Fragment = MapFragment()
    }
}
