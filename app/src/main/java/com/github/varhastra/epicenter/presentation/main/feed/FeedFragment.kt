package com.github.varhastra.epicenter.presentation.main.feed


import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.longSnackbar
import com.github.varhastra.epicenter.common.extensions.setRestrictiveCheckListener
import com.github.varhastra.epicenter.data.EventsDataSource
import com.github.varhastra.epicenter.data.PlacesDataSource
import com.github.varhastra.epicenter.data.network.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.interactors.LoadFeedInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlacesInteractor
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.sorting.SortCriterion
import com.github.varhastra.epicenter.domain.model.sorting.SortOrder
import com.github.varhastra.epicenter.presentation.details.DetailsActivity
import com.github.varhastra.epicenter.presentation.main.ToolbarProvider
import com.github.varhastra.epicenter.presentation.places.PlacesActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.sheet_feed.*

class FeedFragment : Fragment(), FeedContract.View {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var presenter: FeedContract.Presenter

    private lateinit var feedAdapter: FeedAdapter

    private var toolbarProvider: ToolbarProvider? = null

    private val eventsRepository = EventsDataSource.getInstance(UsgsServiceProvider())

    private val placesRepository = PlacesDataSource.getInstance()

    private val locationProvider = LocationProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        FeedPresenter(
                App.instance,
                this,
                LoadFeedInteractor(eventsRepository, locationProvider),
                LoadPlacesInteractor(placesRepository),
                LoadPlaceInteractor(placesRepository)
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetBehavior = BottomSheetBehavior.from(filtersSheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        feedAdapter = FeedAdapter(requireActivity()).apply {
            setHasStableIds(false)
            onItemClickListener = { eventId, _ -> presenter.openEventDetails(eventId) }
        }

        feedRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = feedAdapter
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolbarProvider = (context as ToolbarProvider).apply {
            attachListener { place ->
                presenter.setPlaceAndReload(place.id)
            }
            attachOnEditListener {
                presenter.openPlacesEditor()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.start()

        magnitudeChipGroup.setRestrictiveCheckListener { _, checkedId ->
            val minMag = when (checkedId) {
                R.id.magnitudeZeroChip -> MagnitudeLevel.ZERO_OR_LESS
                R.id.magnitudeTwoChip -> MagnitudeLevel.TWO
                R.id.magnitudeFourChip -> MagnitudeLevel.FOUR
                R.id.magnitudeSixChip -> MagnitudeLevel.SIX
                R.id.magnitudeEightChip -> MagnitudeLevel.EIGHT
                else -> MagnitudeLevel.ZERO_OR_LESS
            }
            presenter.setMinMagnitude(minMag)
        }

        sortingChipGroup.setRestrictiveCheckListener { _, checkedId ->
            val sortCriterion = when (checkedId) {
                R.id.sortByDateChip -> SortCriterion.DATE
                R.id.sortByMagnitudeChip -> SortCriterion.MAGNITUDE
                R.id.sortByDistanceChip -> SortCriterion.DISTANCE
                else -> SortCriterion.DATE
            }
            val sortOrder = when (checkedId) {
                R.id.sortByDateChip -> SortOrder.DESCENDING
                R.id.sortByMagnitudeChip -> SortOrder.DESCENDING
                R.id.sortByDistanceChip -> SortOrder.ASCENDING
                else -> SortOrder.ASCENDING
            }
            presenter.setSortCriterion(sortCriterion)
            presenter.setSortOrder(sortOrder)
        }
    }

    override fun onDetach() {
        super.onDetach()
        toolbarProvider = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_HIDDEN
                    else -> BottomSheetBehavior.STATE_EXPANDED
                }
                true
            }
            R.id.action_refresh -> {
                presenter.refreshEvents()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_DETAILS -> presenter.ignoreUpcomingStartCall()
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun attachPresenter(presenter: FeedContract.Presenter) {
        this.presenter = presenter
    }

    override fun isActive() = isAdded


    override fun showTitle() {
        toolbarProvider?.showDropdown(true)
    }

    override fun showProgress(active: Boolean) {
        if (active) progressBar.show() else progressBar.hide()
    }

    override fun showCurrentPlace(place: Place) {
        toolbarProvider?.setDropdownText(place.name)
    }

    override fun showCurrentSortCriterion(sortCriterion: SortCriterion) {
        val id = when (sortCriterion) {
            SortCriterion.DATE -> R.id.sortByDateChip
            SortCriterion.MAGNITUDE -> R.id.sortByMagnitudeChip
            SortCriterion.DISTANCE -> R.id.sortByDistanceChip
        }
        sortingChipGroup.check(id)
    }

    override fun showCurrentSortOrder(sortOrder: SortOrder) {
        // TODO: implement when ui is ready
    }

    override fun showCurrentMagnitudeFilter(magnitudeLevel: MagnitudeLevel) {
        val id = when (magnitudeLevel) {
            MagnitudeLevel.ZERO_OR_LESS -> R.id.magnitudeZeroChip
            MagnitudeLevel.TWO -> R.id.magnitudeTwoChip
            MagnitudeLevel.FOUR -> R.id.magnitudeFourChip
            MagnitudeLevel.SIX -> R.id.magnitudeSixChip
            MagnitudeLevel.EIGHT -> R.id.magnitudeEightChip
            else -> R.id.magnitudeZeroChip
        }
        magnitudeChipGroup.check(id)
    }

    override fun showPlaces(places: List<PlaceViewBlock>) {
        toolbarProvider?.setDropdownData(places)
    }

    override fun showEvents(events: List<EventViewBlock>) {
        emptyView.visibility = View.INVISIBLE
        feedRecyclerView.visibility = View.VISIBLE

        feedAdapter.data = events
        feedRecyclerView.scrollToPosition(0)
        feedRecyclerView.scheduleLayoutAnimation()
    }

    override fun showErrorNoData(errorType: FeedContract.View.ErrorType) {
        feedRecyclerView.visibility = View.INVISIBLE
        emptyView.apply {
            setTitle(errorType.titleResId)
            setCaption(errorType.bodyResId)
            setImageDrawable(errorType.iconResId)
            visibility = View.VISIBLE
        }
    }

    override fun showLocationPermissionRequest(callback: FeedContract.View.PermissionRequestCallback) {
        activity?.apply {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(object : PermissionListener {
                        override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                            callback.onGranted()
                        }

                        override fun onPermissionRationaleShouldBeShown(
                                permission: PermissionRequest?,
                                token: PermissionToken?
                        ) {
                            token?.continuePermissionRequest()
                        }

                        override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                            callback.onDenied()
                        }
                    }).check()
        }
    }

    override fun showErrorLocationNotAvailable() {
        requireView().longSnackbar(R.string.feed_error_location_not_avaliable, R.string.app_settings) {
            showAppSettings()
        }
    }

    override fun showErrorNoConnection() {
        requireView().longSnackbar(R.string.app_error_no_connection)
    }

    override fun showPlacesEditor() {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()
        val intent = Intent(requireContext(), PlacesActivity::class.java)
        startActivity(intent, options)
    }

    override fun showEventDetails(eventId: String) {
        val intent = Intent(requireContext(), DetailsActivity::class.java).apply {
            putExtra(DetailsActivity.EXTRA_EVENT_ID, eventId)
        }
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity()).toBundle()
        startActivityForResult(intent, REQUEST_DETAILS, options)
    }

    private fun showAppSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        startActivity(intent)
    }


    companion object {
        private const val REQUEST_DETAILS = 100
    }
}
