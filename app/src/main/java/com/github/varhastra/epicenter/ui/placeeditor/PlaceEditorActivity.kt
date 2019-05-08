package com.github.varhastra.epicenter.ui.placeeditor

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindColor
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.StateFragment
import com.github.varhastra.epicenter.data.PlacesRepository
import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.domain.state.placeeditor.PlaceEditorState
import com.github.varhastra.epicenter.ui.placenamepicker.PlaceNamePickerActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_place_editor.*
import kotlinx.android.synthetic.main.sheet_place_editor.*
import org.jetbrains.anko.*


class PlaceEditorActivity : AppCompatActivity(), OnMapReadyCallback, PlaceEditorContract.View {

    private val logger = AnkoLogger(this.javaClass)
    private var map: GoogleMap? = null
    private var areaCircle: Circle? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>
    private lateinit var presenter: PlaceEditorContract.Presenter
    private lateinit var stateFragment: StateFragment<PlaceEditorState>

    @BindColor(R.color.colorSelectedArea)
    @JvmField
    @ColorInt
    var areaColor: Int = 0

    @BindColor(R.color.colorSelectedAreaStroke)
    @JvmField
    @ColorInt
    var areaStrokeColor: Int = 0

    private val markerDragListener = object : GoogleMap.OnMarkerDragListener {
        override fun onMarkerDragEnd(marker: Marker) {
            presenter.setAreaCenter(Coordinates(marker.position.latitude, marker.position.longitude))
            areaCircle?.center = marker.position
        }

        override fun onMarkerDragStart(marker: Marker) {
            // Do nothing
        }

        override fun onMarkerDrag(marker: Marker) {
            areaCircle?.center = marker.position
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_editor)
        ButterKnife.bind(this)

        @Suppress("UNCHECKED_CAST")
        stateFragment = supportFragmentManager.findFragmentByTag(TAG_STATE_FRAGMENT) as? StateFragment<PlaceEditorState>
                ?: StateFragment<PlaceEditorState>().apply {
                    supportFragmentManager.beginTransaction().add(this, TAG_STATE_FRAGMENT).commit()
                }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        nextFab.hide(false)
        nextFab.setOnClickListener { presenter.openNamePicker() }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetRootView)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    nextFab.show(true)
                    bottomSheetBehavior.isHideable = false
                }
            }
        })

        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // Update area radius
                if (fromUser) {
                    presenter.setAreaRadius(progress, false)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Update area radius
                presenter.setAreaRadius(seekBar.progress, true)
            }
        })

        val presenter = PlaceEditorPresenter(this, PlacesRepository.getInstance(), Prefs.getPreferredUnits())
        if (stateFragment.data != null) {
            presenter.initialize(stateFragment.data!!)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        stateFragment.data = presenter.state
    }

    override fun onMapReady(googleMap: GoogleMap) {
        logger.info("onMapReady")
        googleMap.uiSettings.isMapToolbarEnabled = false
        try {
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style
                    )
            )

            if (!success) {
                error("Error parsing map styles.")
            }
        } catch (e: Resources.NotFoundException) {
            error("Map style resource not found. ${e.stackTrace}.")
        }

        googleMap.setOnMapClickListener {
            presenter.createArea(Coordinates(it.latitude, it.longitude))
        }
        googleMap.setOnMarkerDragListener(markerDragListener)

        this.map = googleMap

        presenter.start()
    }

    override fun attachPresenter(presenter: PlaceEditorContract.Presenter) {
        this.presenter = presenter
    }

    override fun setMaxRadiusValue(maxRadius: Int) {
        radiusSeekBar.max = maxRadius
    }

    override fun drawAreaCenter(coordinates: Coordinates) {
        map?.apply {
            val markerOptions = MarkerOptions()
                    .position(LatLng(coordinates.latitude, coordinates.longitude))
                    .draggable(true)
            addMarker(markerOptions)
        }
    }

    override fun drawArea(coordinates: Coordinates, radiusMeters: Double) {
        map?.apply {
            toast(String.format("%x", areaColor))
            val circleOptions = CircleOptions()
                    .center(LatLng(coordinates.latitude, coordinates.longitude))
                    .radius(radiusMeters)
                    .fillColor(areaColor)
                    .strokeColor(areaStrokeColor)
                    .strokeWidth(dip(2).toFloat())
                    .visible(true)
            areaCircle = addCircle(circleOptions)
        }
    }

    override fun updateAreaRadius(radiusMeters: Double) {
        areaCircle?.radius = radiusMeters
    }

    override fun showAreaRadiusText(radiusText: String) {
        radiusTextView.text = radiusText
    }

    override fun setRadius(value: Int) {
        radiusSeekBar.progress = value
    }

    override fun showRadiusControls(show: Boolean) {
        if (show) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun adjustCameraToFitBounds(left: Coordinates, right: Coordinates) {
        val l = LatLng(left.latitude, left.longitude)
        val r = LatLng(right.latitude, right.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(LatLngBounds(l, r), dip(16))
        map?.animateCamera(cameraUpdate)
    }

    override fun showNamePicker(coordinates: Coordinates) {
        startActivityForResult<PlaceNamePickerActivity>(
                REQUEST_PLACE_NAME,
                PlaceNamePickerActivity.EXTRA_LAT to coordinates.latitude,
                PlaceNamePickerActivity.EXTRA_LNG to coordinates.longitude
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PLACE_NAME -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val name = data.getStringExtra(PlaceNamePickerActivity.RESULT_NAME)
                    presenter.onResult(name)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun navigateBack() {
        onBackPressed()
    }

    companion object {
        const val EXTRA_PLACE_ID = "EXTRA_PLACE_ID"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val TAG_STATE_FRAGMENT = "STATE_FRAGMENT"
        const val REQUEST_PLACE_NAME: Int = 100
    }
}
