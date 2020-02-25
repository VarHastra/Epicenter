package com.github.varhastra.epicenter.presentation.main.feed


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.github.varhastra.epicenter.App
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.common.extensions.longSnackbar
import com.github.varhastra.epicenter.common.extensions.setRestrictiveCheckListener
import com.github.varhastra.epicenter.data.EventsDataSource
import com.github.varhastra.epicenter.data.FeedState
import com.github.varhastra.epicenter.data.PlacesDataSource
import com.github.varhastra.epicenter.data.network.usgs.UsgsServiceProvider
import com.github.varhastra.epicenter.device.LocationProvider
import com.github.varhastra.epicenter.domain.interactors.LoadFeedInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlaceInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadPlacesInteractor
import com.github.varhastra.epicenter.domain.interactors.LoadSelectedPlaceNameInteractor
import com.github.varhastra.epicenter.domain.model.filters.MagnitudeLevel
import com.github.varhastra.epicenter.domain.model.sorting.SortCriterion
import com.github.varhastra.epicenter.domain.model.sorting.SortOrder
import com.github.varhastra.epicenter.presentation.details.DetailsActivity
import com.github.varhastra.epicenter.presentation.main.ToolbarProvider
import com.github.varhastra.epicenter.presentation.places.PlacesActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.sheet_feed.*

class FeedFragment : Fragment(), FeedContract.View {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    private lateinit var presenter: FeedContract.Presenter

    private lateinit var feedAdapter: FeedAdapter

    private val locationChipGroupListener: (ChipGroup, Int) -> Unit = { _, checkedId ->
        presenter.setPlaceAndReload(checkedId)
    }

    private val eventsRepository = EventsDataSource.getInstance(UsgsServiceProvider())

    private val placesRepository = PlacesDataSource.getInstance()

    private val locationProvider = LocationProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        FeedPresenter(
                App.instance,
                this,
                LoadSelectedPlaceNameInteractor(FeedState, placesRepository),
                LoadFeedInteractor(eventsRepository, locationProvider),
                LoadPlacesInteractor(placesRepository),
                LoadPlaceInteractor(placesRepository),
                locationProvider
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

        editLocationBtn.setOnClickListener {
            presenter.openPlacesEditor()
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.start()

        locationChipGroup.setRestrictiveCheckListener(locationChipGroupListener)

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

    override fun showProgress(active: Boolean) {
        if (active) progressBar.show() else progressBar.hide()
    }

    override fun showSelectedPlaceName(name: String) {
        (requireActivity() as ToolbarProvider).setTitleText(name)
    }

    override fun showSelectedPlace(placeId: Int) {
        if (placeId == locationChipGroup.checkedChipId) {
            return
        }
        locationChipGroup.check(placeId)
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
        locationChipGroup.apply {
            setOnCheckedChangeListener(null)
            removeAllViews()
            clearCheck()
            setRestrictiveCheckListener(locationChipGroupListener)
        }
        places.map { createLocationChipFor(it) }.forEach { locationChipGroup.addView(it) }
    }

    private fun createLocationChipFor(place: PlaceViewBlock): Chip {
        return (layoutInflater.inflate(R.layout.layout_location_chip, null) as Chip).apply {
            id = place.id
            text = place.titleText
            place.iconResId?.let { iconResId ->
                chipIcon = requireContext().getDrawable(iconResId)
            }
            isChipIconVisible = true
        }
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

    override fun showErrorLocationNotAvailable() {
        requireView().longSnackbar(R.string.feed_error_location_not_avaliable, R.string.app_settings) {
            showAppSettings()
        }
    }

    override fun showErrorNoConnection() {
        requireView().longSnackbar(R.string.app_error_no_connection)
    }

    override fun showPlacesEditor() {
        PlacesActivity.start(requireActivity())
    }

    override fun showEventDetails(eventId: String) {
        DetailsActivity.start(requireActivity(), eventId, REQUEST_DETAILS)
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

        fun newInstance(context: Context): FeedFragment {
            val transitionInflater = TransitionInflater.from(context)
            val enterAnim = transitionInflater.inflateTransition(R.transition.transition_main_enter)
            val exitAnim = transitionInflater.inflateTransition(R.transition.transition_main_exit)
            return FeedFragment().apply {
                enterTransition = enterAnim
                exitTransition = exitAnim
            }
        }
    }
}
