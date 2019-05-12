package com.github.varhastra.epicenter.ui.placeeditor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.transition.TransitionManager
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
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

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                nextFab.show()
                bottomSheetBehavior.isHideable = false
                map?.setPadding(0, dip(56 + 16), 0, bottomSheet.height)
            }
        }
    }

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_editor)
        ButterKnife.bind(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        @Suppress("UNCHECKED_CAST")
        stateFragment = supportFragmentManager.findFragmentByTag(TAG_STATE_FRAGMENT) as? StateFragment<PlaceEditorState>
                ?: StateFragment<PlaceEditorState>().apply {
                    supportFragmentManager.beginTransaction().add(this, TAG_STATE_FRAGMENT).commit()
                }

        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .add(R.id.mapContainer, mapFragment)
                .commit()
        mapFragment.getMapAsync(this)


        nextFab.hide()
        nextFab.setOnClickListener { presenter.openNamePicker() }

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetRootView)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback)

        radiusSeekBar.setOnSeekBarChangeListener(seekBarListener)

        val presenter = PlaceEditorPresenter(this, PlacesRepository.getInstance(), Prefs.getPreferredUnits())
        if (stateFragment.data != null) {
            presenter.initialize(stateFragment.data!!)
        } else {
            val placeId = intent.getIntExtra(EXTRA_PLACE_ID, 0)
            if (placeId != 0) {
                presenter.initialize(placeId)
            }
        }
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
        super.onPause()
        stateFragment.data = presenter.state
    }

    override fun onMapReady(googleMap: GoogleMap) {
        logger.info("onMapReady")
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.setPadding(0, dip(56 + 16), 0, 0)
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

    override fun showRequestLocationPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        onGranted.invoke()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        onDenied.invoke()
                    }
                })
                .check()
    }

    override fun attachPresenter(presenter: PlaceEditorContract.Presenter) {
        this.presenter = presenter
    }

    override fun allowNameEditor(allow: Boolean) {
        if (allow) {
            nextFab.setImageResource(R.drawable.ic_next_fab_dark_24px)
            nextFab.setOnClickListener { presenter.openNamePicker() }
        } else {
            nextFab.setImageResource(R.drawable.ic_save_fab_dark_24px)
            nextFab.setOnClickListener { presenter.saveWithName("") }
        }
    }

    override fun setMaxRadiusValue(maxRadius: Int) {
        radiusSeekBar.max = maxRadius
    }

    override fun drawAreaCenter(coordinates: Coordinates, draggable: Boolean) {
        map?.apply {
            val markerOptions = MarkerOptions()
                    .position(LatLng(coordinates.latitude, coordinates.longitude))
                    .draggable(draggable)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_b))
                    .anchor(0.5f, 0.5f)
            addMarker(markerOptions)
        }
    }

    override fun drawArea(coordinates: Coordinates, radiusMeters: Double) {
        map?.apply {
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

    override fun showTooltip(show: Boolean) {
        tooltipTextView.visibility = if (show) View.VISIBLE else View.INVISIBLE
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
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        startActivityForResult(
                intentFor<PlaceNamePickerActivity>(
                        PlaceNamePickerActivity.EXTRA_LAT to coordinates.latitude,
                        PlaceNamePickerActivity.EXTRA_LNG to coordinates.longitude
                ),
                REQUEST_PLACE_NAME,
                options
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PLACE_NAME -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val name = data.getStringExtra(PlaceNamePickerActivity.RESULT_NAME)
                    presenter.saveWithName(name)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun navigateBack() {
        finish()
    }

    companion object {
        const val EXTRA_PLACE_ID = "EXTRA_PLACE_ID"
        const val EXTRA_MODE = "EXTRA_MODE"
        const val TAG_STATE_FRAGMENT = "STATE_FRAGMENT"
        const val REQUEST_PLACE_NAME: Int = 100
    }
}
