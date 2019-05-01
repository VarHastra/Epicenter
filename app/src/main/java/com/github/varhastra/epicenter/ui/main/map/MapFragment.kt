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
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.ChipGroup

/**
 * A [Fragment] subclass that displays a map
 * of recent earthquakes.
 */
class MapFragment : BaseGmapsFragment(), OnMapReadyCallback {

    @BindView(R.id.sheet_map)
    lateinit var filtersBottomSheet: ViewGroup

    @BindView(R.id.cg_map_filters_magnitude)
    lateinit var magnitudeChipGroup: ChipGroup

    @BindView(R.id.sb_map_filters_date)
    lateinit var dateSeekBar: SeekBar

    lateinit var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>

    lateinit var map: GoogleMap

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

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap
        map.uiSettings.isMapToolbarEnabled = false
        try {
            val success = map.setMapStyle(
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
    }
}
