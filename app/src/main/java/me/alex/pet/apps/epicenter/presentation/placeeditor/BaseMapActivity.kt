package me.alex.pet.apps.epicenter.presentation.placeeditor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import me.alex.pet.apps.epicenter.R

abstract class BaseMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView

    fun initMapView(savedInstanceState: Bundle?) {
        val mapViewBundle = savedInstanceState?.getBundle(STATE_MAP_VIEW_BUNDLE)
        mapView = findViewById<MapView>(R.id.map).apply {
            onCreate(mapViewBundle)
        }
    }

    fun loadMapAsync() {
        mapView.getMapAsync(this@BaseMapActivity)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val mapViewBundle = outState.getBundle(STATE_MAP_VIEW_BUNDLE)
                ?: Bundle().also { mapBundle ->
                    outState.putBundle(STATE_MAP_VIEW_BUNDLE, mapBundle)
                }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }


    companion object {
        private const val STATE_MAP_VIEW_BUNDLE = "MAP_VIEW_BUNDLE"
    }
}