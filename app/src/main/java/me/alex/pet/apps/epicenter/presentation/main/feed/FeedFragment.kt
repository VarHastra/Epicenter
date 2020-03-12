package me.alex.pet.apps.epicenter.presentation.main.feed


import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionInflater
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.sheet_feed.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.setRestrictiveCheckListener
import me.alex.pet.apps.epicenter.common.extensions.snackbar
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.domain.model.sorting.SortCriterion
import me.alex.pet.apps.epicenter.domain.model.sorting.SortOrder
import me.alex.pet.apps.epicenter.presentation.details.DetailsActivity
import me.alex.pet.apps.epicenter.presentation.main.ToolbarProvider
import me.alex.pet.apps.epicenter.presentation.places.PlacesActivity
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class FeedFragment : Fragment(), FeedContract.View {

    val presenter: FeedPresenter by inject { parametersOf(this) }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    private lateinit var feedAdapter: FeedAdapter

    private val locationChipGroupListener: (ChipGroup, Int) -> Unit = { _, checkedId ->
        presenter.setPlaceAndReload(checkedId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        // Intentionally do nothing
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

    override fun showError(error: Error) {
        when (error) {
            is Error.PersistentError -> showPersistentError(error)
            is Error.TransientError -> showTransientError(error)
        }
    }

    private fun showPersistentError(error: Error.PersistentError) {
        feedRecyclerView.visibility = View.GONE

        emptyView.apply {
            setTitle(error.titleResId)
            setCaption(error.captionResId)
            setImageDrawable(error.iconResId)

            setButtonVisibility(error.buttonResId != null)
            error.buttonResId?.let { setButtonText(it) }
            setButtonListener { presenter.onResolveError(error) }

            visibility = View.VISIBLE
        }
    }

    private fun showTransientError(error: Error.TransientError) {
        requireView().snackbar(error.titleResId)
    }

    override fun showPlacesEditor() {
        PlacesActivity.start(requireActivity())
    }

    override fun showEventDetails(eventId: String) {
        DetailsActivity.start(requireActivity(), eventId, REQUEST_DETAILS)
    }

    override fun renderLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
        )
    }

    override fun renderLocationSettingsPrompt(resolvableException: ResolvableApiException) {
        resolvableException.startResolutionForResult(requireActivity(), REQUEST_CHANGE_LOCATION_SETTINGS)
    }

    companion object {
        private const val REQUEST_DETAILS = 1
        private const val REQUEST_CHANGE_LOCATION_SETTINGS = 2
        private const val REQUEST_LOCATION_PERMISSION = 3

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
