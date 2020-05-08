package me.alex.pet.apps.epicenter.presentation.main.feed


import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.fragment_feed.*
import kotlinx.android.synthetic.main.sheet_feed.*
import me.alex.pet.apps.epicenter.R
import me.alex.pet.apps.epicenter.common.extensions.observe
import me.alex.pet.apps.epicenter.common.extensions.setRestrictiveCheckListener
import me.alex.pet.apps.epicenter.common.extensions.snackbar
import me.alex.pet.apps.epicenter.common.functionaltypes.Either
import me.alex.pet.apps.epicenter.domain.model.PlaceName
import me.alex.pet.apps.epicenter.domain.model.filters.MagnitudeLevel
import me.alex.pet.apps.epicenter.domain.model.sorting.SortCriterion
import me.alex.pet.apps.epicenter.presentation.common.navigation.Navigator
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedFragment : Fragment() {

    private val model: FeedModel by viewModel()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    private lateinit var feedAdapter: FeedAdapter

    private val locationChipGroupListener: (ChipGroup, Int) -> Unit = { _, checkedId ->
        model.onChangePlace(checkedId)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        observeModel()
    }

    private fun setUpViews() {
        toolbar.inflateMenu(R.menu.menu_main)

        bottomSheetBehavior = BottomSheetBehavior.from(filtersSheet).apply {
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        feedAdapter = FeedAdapter(requireActivity()).apply {
            setHasStableIds(false)
        }

        feedRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(activity)
            adapter = feedAdapter
        }
    }

    private fun observeModel() = with(model) {
        eventsData.observe(viewLifecycleOwner) { data ->
            when (data) {
                is Either.Success -> renderEvents(data.data)
                is Either.Failure -> renderPersistentError(data.failureDetails)
            }
        }
        places.observe(viewLifecycleOwner, ::renderPlaces)

        selectedPlace.observe(viewLifecycleOwner, ::renderSelectedPlace)
        sortCriterion.observe(viewLifecycleOwner, ::renderSortCriterion)
        minMagnitude.observe(viewLifecycleOwner, ::renderMinMagnitude)

        isLoading.observe(viewLifecycleOwner, ::renderProgressBar)

        toggleFiltersEvent.observe(viewLifecycleOwner) { event ->
            event.consume { renderFilters() }
        }

        adjustLocationSettingsEvent.observe(viewLifecycleOwner) { event ->
            event.consume { renderLocationSettingsPrompt(it) }
        }
        requestLocationPermissionEvent.observe(viewLifecycleOwner) { event ->
            event.consume { renderLocationPermissionRequest() }
        }

        transientErrorEvent.observe(viewLifecycleOwner) { event ->
            event.consume { renderTransientError(it) }
        }

        navigationEvent.observe(viewLifecycleOwner) { event ->
            event.consume { command ->
                (requireActivity() as Navigator).processNavCommand(command)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        model.onStart()

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_filter -> {
                    model.onToggleFiltersVisibility()
                    true
                }
                R.id.action_refresh -> {
                    model.onRefreshEvents()
                    true
                }
                R.id.action_settings -> {
                    model.onOpenSettings()
                    true
                }
                else -> false
            }
        }

        feedAdapter.onItemClickListener = { eventId, _ -> model.onOpenDetails(eventId) }

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
            model.onChangeMinMagnitude(minMag)
        }

        sortingChipGroup.setRestrictiveCheckListener { _, checkedId ->
            val sortCriterion = when (checkedId) {
                R.id.sortByDateChip -> SortCriterion.DATE
                R.id.sortByMagnitudeChip -> SortCriterion.MAGNITUDE
                R.id.sortByDistanceChip -> SortCriterion.DISTANCE
                else -> SortCriterion.DATE
            }
            model.onChangeSortCriterion(sortCriterion)
        }

        editLocationBtn.setOnClickListener {
            model.onOpenPlaceEditor()
        }
    }

    private fun renderProgressBar(isLoading: Boolean) {
        if (isLoading) progressBar.show() else progressBar.hide()
    }

    private fun renderSelectedPlace(place: PlaceName) {
        toolbar.title = place.name
        if (place.id == locationChipGroup.checkedChipId) {
            return
        }
        locationChipGroup.check(place.id)
    }

    private fun renderSortCriterion(sortCriterion: SortCriterion) {
        val id = when (sortCriterion) {
            SortCriterion.DATE -> R.id.sortByDateChip
            SortCriterion.MAGNITUDE -> R.id.sortByMagnitudeChip
            SortCriterion.DISTANCE -> R.id.sortByDistanceChip
        }
        sortingChipGroup.check(id)
    }

    private fun renderMinMagnitude(magnitudeLevel: MagnitudeLevel) {
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

    private fun renderPlaces(places: List<PlaceViewBlock>) {
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

    private fun renderEvents(events: List<EventViewBlock>) {
        emptyView.visibility = View.GONE
        feedRecyclerView.visibility = View.VISIBLE

        feedAdapter.data = events
        feedRecyclerView.scheduleLayoutAnimation()
    }

    private fun renderPersistentError(error: Error.PersistentError) {
        feedRecyclerView.visibility = View.GONE

        emptyView.apply {
            setTitle(error.titleResId)
            setCaption(error.captionResId)
            setImageDrawable(error.iconResId)

            setButtonVisibility(error.buttonResId != null)
            error.buttonResId?.let { setButtonText(it) }
            setButtonListener { model.onResolveError(error) }

            visibility = View.VISIBLE
        }
    }

    private fun renderTransientError(error: Error.TransientError) {
        requireView().snackbar(error.titleResId)
    }

    private fun renderFilters() {
        bottomSheetBehavior.state = when (bottomSheetBehavior.state) {
            BottomSheetBehavior.STATE_HIDDEN -> BottomSheetBehavior.STATE_EXPANDED
            else -> BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun renderLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
        )
    }

    private fun renderLocationSettingsPrompt(resolvableException: ResolvableApiException) {
        resolvableException.startResolutionForResult(requireActivity(), REQUEST_CHANGE_LOCATION_SETTINGS)
    }

    companion object {
        private const val REQUEST_CHANGE_LOCATION_SETTINGS = 2
        private const val REQUEST_LOCATION_PERMISSION = 3

        fun newInstance(): Fragment = FeedFragment()
    }
}