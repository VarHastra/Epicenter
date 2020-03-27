package me.alex.pet.apps.epicenter.presentation.details

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.android.synthetic.main.activity_details.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.common.extensions.setTextColorRes
import me.alex.pet.apps.epicenter.presentation.common.EventMarker
import me.alex.pet.apps.epicenter.presentation.common.toMarkerOptions
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val model: DetailsModel by viewModel { parametersOf(intent.extras!!.getString(EXTRA_EVENT_ID)!!) }

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setUpAnimations()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment).run {
            getMapAsync(this@DetailsActivity)
        }
    }

    private fun setUpAnimations() {
        val inTransition = TransitionInflater.from(this).inflateTransition(R.transition.transition_details_enter)
        val outTransition = TransitionInflater.from(this).inflateTransition(R.transition.transition_details_return)
        with(window) {
            enterTransition = inTransition
            returnTransition = outTransition
        }
    }

    override fun onStart() {
        super.onStart()
        sourceLinkTile.setOnClickListener { model.onVisitSource() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            setMapStyle(MapStyleOptions.loadRawResourceStyle(this@DetailsActivity, R.raw.map_style))
        }

        observeModel()
    }

    private fun observeModel() = with(model) {
        eventViewBlock.observe(this@DetailsActivity, ::renderEventDetails)
        eventMarker.observe(this@DetailsActivity, ::renderMarker)
        visitSourceLinkEvent.observe(this@DetailsActivity) { event ->
            event.consume { uri -> openSourceLink(uri) }
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

    private fun renderEventDetails(event: EventViewBlock) {
        magnitudeValueTv.text = event.magnitudeValue
        magnitudeTypeTv.text = event.magnitudeType

        magnitudeValueTv.setTextColorRes(event.alertLevel.colorResId)
        magnitudeTypeTv.setTextColorRes(event.alertLevel.colorResId)

        titleTv.text = event.title

        locationTile.setFirstLineText(event.coordinates)
        locationTile.setSecondLineText(event.distanceFromUser)

        dateTimeTile.setFirstLineText(event.dateTime)
        dateTimeTile.setSecondLineText(event.daysAgo)

        depthTile.setText(event.depth)

        feltReportsTile.setText(event.feltReports)

        sourceLinkTile.setText(event.sourceLink)

        tsunamiAlertTv.visibility = if (event.tsunamiAlert) View.VISIBLE else View.GONE
    }

    private fun renderMarker(marker: EventMarker) {
        map.run {
            addMarker(marker.toMarkerOptions())
            moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            LatLng(marker.latitude, marker.longitude),
                            2.0f
                    )
            )
        }
    }

    private fun openSourceLink(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    companion object {
        private const val EXTRA_EVENT_ID = "EVENT_ID"

        fun start(sourceActivity: Activity, eventId: String, requestCode: Int = -1) {
            val intent = Intent(sourceActivity, DetailsActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(sourceActivity).toBundle()
            sourceActivity.startActivityForResult(intent, requestCode, options)
        }
    }
}