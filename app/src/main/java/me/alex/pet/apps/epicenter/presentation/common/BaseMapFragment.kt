package me.alex.pet.apps.epicenter.presentation.common


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import me.alex.pet.apps.epicenter.R


abstract class BaseMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapViewBundle = savedInstanceState?.getBundle(STATE_MAP_VIEW_BUNDLE)
        mapView = view.findViewById<MapView>(R.id.map).apply {
            onCreate(mapViewBundle)
            getMapAsync(this@BaseMapFragment)
        }
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

        val mapViewBundle = outState.getBundle(STATE_MAP_VIEW_BUNDLE) ?: Bundle().also {
            outState.putBundle(STATE_MAP_VIEW_BUNDLE, it)
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
