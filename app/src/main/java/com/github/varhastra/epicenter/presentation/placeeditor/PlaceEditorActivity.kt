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

    private lateinit var areaCircle: Circle

    private lateinit var presenter: PlaceEditorContract.Presenter

    @BindColor(R.color.colorSelectedArea)
    @JvmField
    @ColorInt
    var areaColor: Int = 0

    @BindColor(R.color.colorSelectedAreaStroke)
    @JvmField
    @ColorInt
    var areaStrokeColor: Int = 0


    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // Update area radius
            if (fromUser) {
                presenter.onChangeAreaRadius(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // Do nothing
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            // Update area radius
            presenter.onStopChangingAreaRadius(map.projection.visibleRegion.latLngBounds)
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
            setOnCameraMoveListener(::onMapCameraMove)
        }

        val areaCircleOptions = CircleOptions()
                .center(LatLng(0.0, 0.0))
                .radius(1.0)
                .fillColor(areaColor)
                .strokeColor(areaStrokeColor)
                .strokeWidth(dip(2).toFloat())
                .visible(false)
        this.areaCircle = googleMap.addCircle(areaCircleOptions)

        presenter.start()
    }

    private fun onMapCameraMove() {
        val cameraTarget = map.cameraPosition.target
        presenter.onChangeAreaCenter(cameraTarget)
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

    override fun renderArea(center: LatLng, radiusMeters: Double) {
        areaCircle.let {
            it.center = center
            it.radius = radiusMeters
            it.isVisible = true
        }
    }

    override fun showAreaRadiusText(radiusText: String) {
        radiusTextView.text = radiusText
    }

    override fun showRadius(percentage: Int) {
        radiusSeekBar.progress = percentage
    }

    override fun adjustCameraToFitBounds(bounds: LatLngBounds, animate: Boolean) {
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, dip(16))
        if (animate) {
            map.animateCamera(cameraUpdate)
        } else {
            map.moveCamera(cameraUpdate)
        }
    }

    override fun showNamePicker(latLng: LatLng) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()
        startActivityForResult(
                intentFor<PlaceNamePickerActivity>(
                        PlaceNamePickerActivity.EXTRA_LAT to latLng.latitude,
                        PlaceNamePickerActivity.EXTRA_LNG to latLng.longitude
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
