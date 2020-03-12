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
import me.alex.pet.apps.epicenter.common.extensions.setTextColorRes
import me.alex.pet.apps.epicenter.presentation.common.EventMarker
import me.alex.pet.apps.epicenter.presentation.common.toMarkerOptions
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class DetailsActivity : AppCompatActivity(), DetailsContract.View, OnMapReadyCallback {

    val presenter: DetailsPresenter by inject { parametersOf(this) }

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        setUpAnimations()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sourceLinkTile.setOnClickListener { presenter.openSourceLink() }

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
                ?: throw IllegalStateException("Event id was expected as an intent extra with the DetailsActivity.EXTRA_EVENT_ID key.")
        presenter.init(eventId)
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
        (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment).run {
            getMapAsync(this@DetailsActivity)
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

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, null)
        super.onBackPressed()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            setMapStyle(MapStyleOptions.loadRawResourceStyle(this@DetailsActivity, R.raw.map_style))
        }

        presenter.start()
    }

    override fun attachPresenter(presenter: DetailsContract.Presenter) {
        // Intentionally do nothing
    }

    override fun isActive() = !(isFinishing || isDestroyed)

    override fun showEvent(event: EventViewBlock) {
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

    override fun showEventMapMarker(marker: EventMarker) {
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

    override fun showErrorNoData() {
        // TODO: implement
    }

    override fun openSourceLink(uri: Uri) {
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