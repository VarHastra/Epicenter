package me.alex.pet.apps.epicenter.presentation.placeeditor

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
import kotlinx.android.synthetic.main.activity_place_editor.*
import kotlinx.android.synthetic.main.layout_place_editor_controls.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.getColorCompat
import me.alex.pet.apps.epicenter.presentation.placenamepicker.PlaceNamePickerActivity
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class PlaceEditorActivity : BaseMapActivity(), OnMapReadyCallback, PlaceEditorContract.View {

    val presenter: PlaceEditorPresenter by inject { parametersOf(this) }

    private lateinit var map: GoogleMap

    private lateinit var areaCircle: Circle


    private val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                presenter.onChangeAreaRadius(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // Do nothing
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            presenter.onStopChangingAreaRadius(map.projection.visibleRegion.latLngBounds)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_editor)

        initMapView(savedInstanceState)
        setUpViews()

        if (savedInstanceState != null) {
            presenter.onRestoreState(savedInstanceState)
        } else {
            val placeId = if (intent.hasExtra(EXTRA_PLACE_ID)) intent.getIntExtra(EXTRA_PLACE_ID, 0) else null
            presenter.initialize(placeId)
        }
    }

    private fun setUpViews() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        nextFab.setOnClickListener { presenter.openNamePicker() }

        radiusSeekBar.setOnSeekBarChangeListener(seekBarListener)
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
            setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this@PlaceEditorActivity, R.raw.map_style
                    )
            )
            setOnCameraMoveListener(::onMapCameraMove)
        }

        val areaCircleOptions = createAreaCircleOptions()
        this.areaCircle = googleMap.addCircle(areaCircleOptions)

        presenter.start()
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
        presenter.onChangeAreaCenter(cameraTarget)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        presenter.onSaveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun attachPresenter(presenter: PlaceEditorContract.Presenter) {
        // Intentionally do nothing
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
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 16.dp)
        if (animate) {
            map.animateCamera(cameraUpdate)
        } else {
            map.moveCamera(cameraUpdate)
        }
    }

    override fun showNamePicker(latLng: LatLng) {
        PlaceNamePickerActivity.start(this, latLng, REQUEST_PLACE_NAME)
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


    private val Int.dp
        get() = (this * resources.displayMetrics.density).toInt()

    companion object {
        private const val EXTRA_PLACE_ID = "EXTRA_PLACE_ID"
        private const val REQUEST_PLACE_NAME: Int = 100

        fun start(sourceActivity: Activity, placeId: Int? = null) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(sourceActivity).toBundle()
            val intent = Intent(sourceActivity, PlaceEditorActivity::class.java)
            placeId?.let { intent.putExtra(EXTRA_PLACE_ID, it) }
            sourceActivity.startActivity(intent, options)
        }
    }
}
