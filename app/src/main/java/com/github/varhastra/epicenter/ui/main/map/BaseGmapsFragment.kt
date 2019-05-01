package com.github.varhastra.epicenter.ui.main.map


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.MapView


/**
 * A simple [Fragment] subclass that is designed to hold a [MapView].
 * This fragment forwards lifecycle methods to the corresponding
 * methods in the [MapView].
 * Subclasses must call [onCreatingMapView] from their [onCreateView] method.
 */
open class BaseGmapsFragment : Fragment() {

    protected lateinit var mapView: MapView
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * Call this method from [onCreateView].
     */
    fun onCreatingMapView(mapView: MapView, savedInstanceState: Bundle?) {
        this.mapView = mapView
        val mapViewStateBundle = savedInstanceState?.getBundle(STATE_MAP_VIEW_BUNDLE)
        mapView.onCreate(mapViewStateBundle)
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

        var mapViewStateBundle = outState.getBundle(STATE_MAP_VIEW_BUNDLE)
        if (mapViewStateBundle == null) {
            mapViewStateBundle = Bundle()
            outState.putBundle(STATE_MAP_VIEW_BUNDLE, mapViewStateBundle)
        }

        mapView.onSaveInstanceState(mapViewStateBundle)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val STATE_MAP_VIEW_BUNDLE = "STATE_MAP_VIEW_BUNDLE"
    }
}
