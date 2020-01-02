package com.github.varhastra.epicenter.presentation.details

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import butterknife.BindColor
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.EventsRepository
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.interactors.EventLoaderInteractor
import com.github.varhastra.epicenter.domain.model.Coordinates
import com.github.varhastra.epicenter.utils.UnitsFormatter
import com.github.varhastra.epicenter.views.TileTwolineView
import com.github.varhastra.epicenter.views.TileView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.AnkoLogger
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.DecimalFormat

class DetailsActivity : AppCompatActivity(), DetailsContract.View, OnMapReadyCallback {

    val logger = AnkoLogger(this.javaClass)

    @JvmField
    @BindColor(R.color.colorAlert0)
    @ColorInt
    var colorAlert0: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert2)
    @ColorInt
    var colorAlert2: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert4)
    @ColorInt
    var colorAlert4: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert6)
    @ColorInt
    var colorAlert6: Int = 0

    @JvmField
    @BindColor(R.color.colorAlert8)
    @ColorInt
    var colorAlert8: Int = 0

    @BindView(R.id.tb_details)
    lateinit var toolbar: Toolbar

    @BindView(R.id.scrollview_details)
    lateinit var scrollView: NestedScrollView

    @BindView(R.id.tile_details_datetime)
    lateinit var dateTile: TileTwolineView

    @BindView(R.id.tile_depth_datetime)
    lateinit var depthTile: TileView

    @BindView(R.id.tile_details_dyfi)
    lateinit var dyfiTile: TileView

    @BindView(R.id.tile_details_source_link)
    lateinit var sourceTile: TileView

    @BindView(R.id.tv_details_header_magnitude)
    lateinit var magnitudeTextView: TextView

    @BindView(R.id.tv_details_header_magnitude_type)
    lateinit var magnitudeTypeTextView: TextView

    @BindView(R.id.tv_details_place_name)
    lateinit var placeNameTextView: TextView

    @BindView(R.id.tv_details_distance)
    lateinit var distanceTextView: TextView

    @BindView(R.id.tv_details_coordinates)
    lateinit var coordinatesTextView: TextView

    @BindView(R.id.tv_details_tsunami_alert)
    lateinit var tsunamiAlertTextView: TextView

    private lateinit var presenter: DetailsContract.Presenter
    private lateinit var map: GoogleMap

    private var alertAccentColor: Int = 0

    private val magFormatter: DecimalFormat = DecimalFormat("0.0")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        ButterKnife.bind(this)
        setUpAnimations()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sourceTile.setOnClickListener { presenter.openSourceLink() }


        val presenter = DetailsPresenter(this, EventLoaderInteractor(EventsRepository.getInstance(), LocationProvider()))
        intent?.apply {
            val eventId = getStringExtra(EXTRA_EVENT_ID)
            presenter.init(eventId)
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

    override fun onResume() {
        super.onResume()
        presenter.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, null)
        super.onBackPressed()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = false
        try {
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style
                    )
            )

            if (!success) {
                error("Error parsing map styles.")
            }
        } catch (e: Resources.NotFoundException) {
            error("Map style resource not found. ${e.stackTrace}.")
        }
        presenter.onMapReady()
    }

    override fun attachPresenter(presenter: DetailsContract.Presenter) {
        this.presenter = presenter
    }

    override fun isActive() = !(isFinishing || isDestroyed)

    override fun showEventOnMap(coordinates: Coordinates, alertType: DetailsContract.View.AlertType) {
        val markerPos = LatLng(coordinates.latitude, coordinates.longitude)

        val marker = MarkerOptions()
                .position(markerPos)
                .icon(BitmapDescriptorFactory.fromResource(getMarkerResource(alertType)))
                .anchor(0.5f, 0.5f)

        map.addMarker(marker)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPos, 2.4f))
    }

    override fun setAlertColor(alertType: DetailsContract.View.AlertType) {
        alertAccentColor = getAlertColor(alertType)
    }

    override fun showEventMagnitude(magnitude: Double, type: String) {
        magnitudeTextView.text = magFormatter.format(magnitude)
        magnitudeTypeTextView.text = type

        magnitudeTextView.setTextColor(alertAccentColor)
        magnitudeTypeTextView.setTextColor(alertAccentColor)
    }

    override fun showEventPlace(place: String) {
        placeNameTextView.text = place
    }

    override fun showEventDistance(distance: Double?, unitsFormatter: UnitsFormatter) {
        distanceTextView.text = getString(R.string.details_event_distance, unitsFormatter.getLocalizedDistanceString(distance?.toInt()))
    }

    override fun showEventCoordinates(coordinates: Coordinates) {
        coordinatesTextView.text = getString(R.string.details_event_coordinates, coordinates.latitude, coordinates.longitude)
        coordinatesTextView.setTextColor(alertAccentColor)
    }

    override fun showTsunamiAlert(show: Boolean) {
        tsunamiAlertTextView.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun showEventDate(localDateTime: LocalDateTime, daysAgo: Int) {
        dateTile.setFirstLineText(dateTimeFormatter.format(localDateTime))
        dateTile.setSecondLineText(resources.getQuantityString(R.plurals.plurals_details_days_ago, daysAgo, daysAgo))
    }

    override fun showEventDepth(depth: Double, unitsFormatter: UnitsFormatter) {
        depthTile.setText(unitsFormatter.getLocalizedDistanceString(depth))
    }

    override fun showEventReports(reportsCount: Int) {
        dyfiTile.setText(reportsCount.toString())
    }

    override fun showEventLink(linkUrl: String) {
        sourceTile.setText(linkUrl)
    }

    override fun showErrorNoData() {
//        TODO("stub, not implemented")
    }

    override fun showSourceLinkViewer(link: String) {
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    @ColorInt
    private fun getAlertColor(alertType: DetailsContract.View.AlertType): Int {
        return when (alertType) {
            DetailsContract.View.AlertType.ALERT_0 -> colorAlert0
            DetailsContract.View.AlertType.ALERT_2 -> colorAlert2
            DetailsContract.View.AlertType.ALERT_4 -> colorAlert4
            DetailsContract.View.AlertType.ALERT_6 -> colorAlert6
            DetailsContract.View.AlertType.ALERT_8 -> colorAlert8
            else -> colorAlert0
        }
    }

    private fun getMarkerResource(alertType: DetailsContract.View.AlertType): Int {
        return when (alertType) {
            DetailsContract.View.AlertType.ALERT_0 -> R.drawable.marker_0
            DetailsContract.View.AlertType.ALERT_2 -> R.drawable.marker_2
            DetailsContract.View.AlertType.ALERT_4 -> R.drawable.marker_4
            DetailsContract.View.AlertType.ALERT_6 -> R.drawable.marker_6
            DetailsContract.View.AlertType.ALERT_8 -> R.drawable.marker_8
            else -> R.drawable.marker_0
        }
    }


    companion object {
        const val EXTRA_EVENT_ID = "EVENT_ID"
    }
}
