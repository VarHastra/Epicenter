package me.alex.pet.apps.epicenter.presentation.placeeditor.locationpicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.fragment_location_picker.*
import kotlinx.android.synthetic.main.layout_place_editor_controls.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.getColorCompat
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.common.extensions.parentViewModel
import me.alex.pet.apps.epicenter.presentation.placeeditor.PlaceEditorModel


class LocationPickerFragment : Fragment(), OnMapReadyCallback {

    private val model: PlaceEditorModel by parentViewModel()

    private lateinit var map: GoogleMap

    private lateinit var areaCircle: Circle

    private var isMapReady = false

    private var isMapLaidOut = false

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_location_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews(view)
    }

    private fun setUpViews(root: View) {
        (childFragmentManager.findFragmentById(R.id.locationPickerMap) as SupportMapFragment).getMapAsync(this)
        toolbar.setNavigationIcon(R.drawable.ic_up)

        val onGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                isMapLaidOut = true
                onMapReadinessChanged()
                root.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        root.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    override fun onStart() {
        super.onStart()
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        nextFab.setOnClickListener { model.onOpenNamePicker() }
        radiusSeekBar.setOnSeekBarChangeListener(seekBarListener)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(),
                            R.raw.map_style
                    )
            )
            setOnCameraMoveListener(::onMapCameraMove)
            clear()
        }
        this.areaCircle = googleMap.addCircle(createAreaCircleOptions())

        isMapReady = true
        onMapReadinessChanged()
    }

    private fun onMapReadinessChanged() {
        // Wait for both conditions to be able to safely call CameraUpdateFactory.newLatLngBounds()
        if (isMapReady && isMapLaidOut) {
            observeModel()
        }
    }

    private fun observeModel() = with(model) {
        areaCenterLatLng.observe(viewLifecycleOwner, ::renderAreaCenter)
        areaRadiusMeters.observe(viewLifecycleOwner, ::renderAreaRadius)
        areaRadiusText.observe(viewLifecycleOwner, ::renderAreaRadiusText)
        areaRadiusPercentage.observe(viewLifecycleOwner, ::renderRadius)

        adjustCameraEvent.observe(viewLifecycleOwner) { event ->
            event.consume { adjustCameraToFitBounds(it.first, it.second) }
        }
    }

    private fun createAreaCircleOptions(): CircleOptions? {
        val areaColor = requireContext().getColorCompat(R.color.colorSelectedArea)
        return CircleOptions()
                .center(LatLng(0.0, 0.0))
                .radius(1.0)
                .fillColor(areaColor)
                .strokeWidth(0f)
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


    private val Int.dp
        get() = (this * resources.displayMetrics.density).toInt()


    companion object {
        fun newInstance(): Fragment = LocationPickerFragment()
    }
}