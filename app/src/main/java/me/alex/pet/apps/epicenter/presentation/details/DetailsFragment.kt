package me.alex.pet.apps.epicenter.presentation.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.android.synthetic.main.fragment_details.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.common.extensions.setTextColorRes
import me.alex.pet.apps.epicenter.presentation.common.EventMarker
import me.alex.pet.apps.epicenter.presentation.common.toMarkerOptions
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DetailsFragment : Fragment(), OnMapReadyCallback {

    private val model: DetailsModel by viewModel {
        val eventId = requireArguments().getString(EXTRA_EVENT_ID)
                ?: throw IllegalStateException("DetailsFragment $this doesn't have required event id argument.")
        parametersOf(eventId)
    }

    private lateinit var map: GoogleMap


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (childFragmentManager.findFragmentById(R.id.detailsMap) as SupportMapFragment).getMapAsync(this)

        toolbar.apply {
            navigationIcon = requireContext().getDrawable(R.drawable.ic_up)
            overflowIcon = requireContext().getDrawable(R.drawable.ic_overflow_menu)
        }
    }

    override fun onStart() {
        super.onStart()

        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        sourceLinkTile.setOnClickListener { model.onVisitSource() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap.apply {
            uiSettings.isMapToolbarEnabled = false
            setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        }

        observeModel()
    }

    private fun observeModel() = with(model) {
        eventViewBlock.observe(viewLifecycleOwner, ::renderEventDetails)
        eventMarker.observe(viewLifecycleOwner, ::renderMarker)
        visitSourceLinkEvent.observe(viewLifecycleOwner) { event ->
            event.consume { uri -> openSourceLink(uri) }
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
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }


    companion object {

        fun newInstance(eventId: String): Fragment {
            return DetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_EVENT_ID, eventId)
                }
            }
        }
    }
}


private const val EXTRA_EVENT_ID = "EVENT_ID"