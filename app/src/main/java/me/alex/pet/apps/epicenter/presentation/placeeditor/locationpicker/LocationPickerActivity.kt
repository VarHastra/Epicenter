package me.alex.pet.apps.epicenter.presentation.placeeditor.locationpicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.core.app.ActivityOptionsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_location_picker.*
import kotlinx.android.synthetic.main.layout_place_editor_controls.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.getColorCompat
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.presentation.placeeditor.namepicker.NamePickerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf


class LocationPickerActivity : BaseMapActivity(), OnMapReadyCallback {

    private val model: LocationPickerModel by viewModel {
        parametersOf(if (intent.hasExtra(EXTRA_PLACE_ID)) intent.getIntExtra(EXTRA_PLACE_ID, 0) else null)
    }

    private lateinit var map: GoogleMap

    private lateinit var areaCircle: Circle

    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                model.onChangeAreaRadius(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // Do nothing
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            model.onStopChangingAreaRadius(map.projection.visibleRegion.latLngBounds)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_picker)

        initMapView(savedInstanceState)
        loadMapAsync()
        setUpViews()

        if (savedInstanceState != null) {
            model.onRestoreState(savedInstanceState)
        }
    }

    private fun setUpViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    override fun onStart() {
        super.onStart()
        nextFab.setOnClickListener { model.onOpenNamePicker() }
        radiusSeekBar.setOnSeekBarChangeListener(seekBarListener)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this@LocationPickerActivity, R.raw.map_style
                    )
            )
            setOnCameraMoveListener(::onMapCameraMove)
        }

        val areaCircleOptions = createAreaCircleOptions()
        this.areaCircle = googleMap.addCircle(areaCircleOptions)

        observeModel()
    }

    private fun observeModel() = with(model) {
        areaCenterLatLng.observe(this@LocationPickerActivity, ::renderAreaCenter)
        areaRadiusMeters.observe(this@LocationPickerActivity, ::renderAreaRadius)
        areaRadiusText.observe(this@LocationPickerActivity, ::renderAreaRadiusText)
        areaRadiusPercentage.observe(this@LocationPickerActivity, ::renderRadius)

        adjustCameraEvent.observe(this@LocationPickerActivity) { event ->
            event.consume { adjustCameraToFitBounds(it.first, it.second) }
        }
        openNamePickerEvent.observe(this@LocationPickerActivity) { event ->
            event.consume { renderNamePicker(it) }
        }
        navigateBackEvent.observe(this@LocationPickerActivity) { event ->
            event.consume { navigateBack() }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PLACE_NAME -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val name = data.getStringExtra(NamePickerActivity.RESULT_NAME)
                    model.onSaveAndExit(name)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        model.onSaveState(outState)
        super.onSaveInstanceState(outState)
    }

    private fun createAreaCircleOptions(): CircleOptions? {
        val areaColor = getColorCompat(R.color.colorSelectedArea)
        val areaStrokeColor = getColorCompat(R.color.colorSelectedAreaStroke)
        return CircleOptions()
                .center(LatLng(0.0, 0.0))
                .radius(1.0)
                .fillColor(areaColor)
                .strokeColor(areaStrokeColor)
                .strokeWidth(2.dp.toFloat())
                .visible(false)
    }

    private fun onMapCameraMove() {
        val cameraTarget = map.cameraPosition.target
        model.onChangeAreaCenter(cameraTarget)
    }

    private fun renderAreaCenter(center: LatLng) {
        if (areaCircle.center == center) {
            return
        }
        areaCircle.let {
            it.center = center
            it.isVisible = true
        }
    }

    private fun renderAreaRadius(radiusInMeters: Double) {
        if (areaCircle.radius == radiusInMeters) {
            return
        }
        areaCircle.let {
            it.radius = radiusInMeters
            it.isVisible = true
        }
    }

    private fun renderAreaRadiusText(text: String) {
        if (radiusTextView.text == text) {
            return
        }
        radiusTextView.text = text
    }

    private fun renderRadius(percentage: Int) {
        if (radiusSeekBar.progress == percentage) {
            return
        }
        radiusSeekBar.progress = percentage
    }

    private fun adjustCameraToFitBounds(bounds: LatLngBounds, animate: Boolean) {
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 16.dp)
        if (animate) {
            map.animateCamera(cameraUpdate)
        } else {
            map.moveCamera(cameraUpdate)
        }
    }

    private fun renderNamePicker(latLng: LatLng) {
        NamePickerActivity.start(this, latLng, REQUEST_PLACE_NAME)
    }

    private fun navigateBack() {
        finish()
    }


    private val Int.dp
        get() = (this * resources.displayMetrics.density).toInt()


    companion object {
        fun start(sourceActivity: Activity, placeId: Int? = null) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(sourceActivity).toBundle()
            val intent = Intent(sourceActivity, LocationPickerActivity::class.java)
            placeId?.let { intent.putExtra(EXTRA_PLACE_ID, it) }
            sourceActivity.startActivity(intent, options)
        }
    }
}

private const val EXTRA_PLACE_ID = "EXTRA_PLACE_ID"
private const val REQUEST_PLACE_NAME: Int = 100
