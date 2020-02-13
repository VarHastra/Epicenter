package com.github.varhastra.epicenter.presentation.placeeditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.core.app.ActivityOptionsCompat
import butterknife.BindColor
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.AppSettings
import com.github.varhastra.epicenter.data.PlacesDataSource
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.presentation.placenamepicker.PlaceNamePickerActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_place_editor.*
import kotlinx.android.synthetic.main.layout_place_editor_controls.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.dip
import org.jetbrains.anko.intentFor


class PlaceEditorActivity : BaseMapActivity(), OnMapReadyCallback, PlaceEditorContract.View {

    private val logger = AnkoLogger(this.javaClass)

    private lateinit var map: GoogleMap
    private var areaCircle: Circle? = null
    private lateinit var presenter: PlaceEditorContract.Presenter

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

        initMapView(savedInstanceState)

        nextFab.setOnClickListener { presenter.openNamePicker() }

        radiusSeekBar.setOnSeekBarChangeListener(seekBarListener)

        val presenter = PlaceEditorPresenter(this, PlacesDataSource.getInstance(), AppSettings.preferredUnits)
        if (savedInstanceState != null) {
            presenter.onRestoreState(savedInstanceState)
            return
        }

        val placeId = if (intent.hasExtra(EXTRA_PLACE_ID)) intent.getIntExtra(EXTRA_PLACE_ID, 0) else null
        presenter.initialize(placeId)
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

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            setPadding(0, dip(56 + 16), 0, 0)
            setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this@PlaceEditorActivity, R.raw.map_style
                    )
            )
            setOnMarkerDragListener(markerDragListener)
        }

        presenter.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        presenter.onSaveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun attachPresenter(presenter: PlaceEditorContract.Presenter) {
        this.presenter = presenter
    }

    override fun loadMap() {
        loadMapAsync()
    }

    override fun setMaxRadiusValue(maxRadius: Int) {
        radiusSeekBar.max = maxRadius
    }

    override fun drawAreaCenter(coordinates: Coordinates) {
        val markerOptions = MarkerOptions()
                .position(LatLng(coordinates.latitude, coordinates.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_b))
                .anchor(0.5f, 0.5f)
        map.addMarker(markerOptions)
    }

    override fun drawArea(coordinates: Coordinates, radiusMeters: Double) {
        val circleOptions = CircleOptions()
                .center(LatLng(coordinates.latitude, coordinates.longitude))
                .radius(radiusMeters)
                .fillColor(areaColor)
                .strokeColor(areaStrokeColor)
                .strokeWidth(dip(2).toFloat())
                .visible(true)
        areaCircle = map.addCircle(circleOptions)
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

    override fun adjustCameraToFitBounds(left: Coordinates, right: Coordinates) {
        val l = LatLng(left.latitude, left.longitude)
        val r = LatLng(right.latitude, right.longitude)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(LatLngBounds(l, r), dip(16))
        map.animateCamera(cameraUpdate)
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
        const val REQUEST_PLACE_NAME: Int = 100
    }
}
