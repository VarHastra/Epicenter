package com.github.varhastra.epicenter.main.feed


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.domain.model.Event
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.main.ToolbarProvider
import com.github.varhastra.epicenter.views.EmptyView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.jetbrains.anko.toast

/**
 * A [Fragment] subclass that displays a list
 * of recent earthquakes.
 */
class FeedFragment : Fragment(), FeedContract.View {

    @BindView(R.id.pb_feed)
    lateinit var progressBar: ContentLoadingProgressBar

    @BindView(R.id.cg_feed_filters_magnitude)
    lateinit var magnitudeChipGroup: ChipGroup

    @BindView(R.id.cg_feed_filters_sort_by)
    lateinit var sortingChipGroup: ChipGroup

    @BindView(R.id.sheet_feed)
    lateinit var sheetFeed: ViewGroup
    lateinit var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>

    @BindView(R.id.emptv_feed)
    lateinit var emptyView: EmptyView

    @BindView(R.id.rv_feed)
    lateinit var feedRecyclerView: RecyclerView

    private lateinit var presenter: FeedContract.Presenter

    private lateinit var feedAdapter: FeedAdapter

    private var toolbarProvider: ToolbarProvider? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_feed, container, false)
        ButterKnife.bind(this, root)

        setHasOptionsMenu(true)

        bottomSheetBehavior = BottomSheetBehavior.from(sheetFeed)
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        feedAdapter = FeedAdapter(activity!!)
        feedRecyclerView.setHasFixedSize(true)
        feedRecyclerView.layoutManager = LinearLayoutManager(activity)
        feedRecyclerView.adapter = feedAdapter

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        toolbarProvider = context as? ToolbarProvider
        toolbarProvider?.attachListener {
            presenter.setPlaceAndReload(it)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.start()

        magnitudeChipGroup.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach {
                (it as? Chip)?.apply {
                    isClickable = !isChecked
                }
            }

            val minMag = when (checkedId) {
                R.id.chip_mag_0 -> 0
                R.id.chip_mag_2 -> 2
                R.id.chip_mag_4 -> 4
                R.id.chip_mag_6 -> 6
                R.id.chip_mag_8 -> 8
                else -> 0
            }
            presenter.setMagnitudeFilterAndReload(minMag)
        }

        sortingChipGroup.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach {
                (it as? Chip)?.apply {
                    isClickable = !isChecked
                }
            }

            val sorting = when (checkedId) {
                R.id.chip_sorting_date -> FeedFilter.Sorting.DATE
                R.id.chip_sorting_mag -> FeedFilter.Sorting.MAGNITUDE
                else -> FeedFilter.Sorting.DATE
            }
            presenter.setSortingAndReload(sorting)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                true
            }
            R.id.action_refresh -> {

                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    override fun attachPresenter(presenter: FeedContract.Presenter) {
        this.presenter = presenter
    }

    override fun isActive() = isAdded


    override fun showProgress(active: Boolean) {
        if (active) progressBar.show() else progressBar.hide()
    }

    override fun showCurrentPlace(place: Place) {
        toolbarProvider?.setDropdownText(place.name)
    }

    override fun showCurrentFilter(filter: FeedFilter) {
        showCurrentMagnitudeFilter(filter.minMagnitude.toInt())
        showCurrentSorting(filter.sorting)
    }

    private fun showCurrentMagnitudeFilter(magnitude: Int) {
        val id = when (magnitude) {
            in -2 until 2 -> R.id.chip_mag_0
            in 2 until 4 -> R.id.chip_mag_2
            in 4 until 6 -> R.id.chip_mag_4
            in 6 until 8 -> R.id.chip_mag_6
            in 8..10 -> R.id.chip_mag_8
            else -> R.id.chip_mag_0
        }
        magnitudeChipGroup.check(id)
    }

    private fun showCurrentSorting(sorting: FeedFilter.Sorting) {
        val id = when (sorting) {
            FeedFilter.Sorting.DATE -> R.id.chip_sorting_date
            FeedFilter.Sorting.MAGNITUDE -> R.id.chip_sorting_mag
        }
        sortingChipGroup.check(id)
    }

    override fun showPlaces(places: List<Place>) {
        toolbarProvider?.setDropdownData(places)
    }

    override fun showEvents(events: List<Event>) {
        emptyView.visibility = View.INVISIBLE
        feedRecyclerView.visibility = View.VISIBLE
        feedAdapter.data = events
    }

    override fun showError(reason: FeedContract.View.ErrorReason) {
        val triple = when (reason) {
            FeedContract.View.ErrorReason.ERR_NO_EVENTS -> Triple(R.string.app_error_no_events, R.string.app_error_no_events_capt, R.drawable.ic_error_earth_24px)
            FeedContract.View.ErrorReason.ERR_NO_CONNECTION -> Triple(R.string.app_error_no_connection, R.string.app_error_no_connection_capt, R.drawable.ic_error_wifi_off_24px)
            FeedContract.View.ErrorReason.ERR_UNKNOWN -> Triple(R.string.app_error_unknown, R.string.app_error_unknown_capt, R.drawable.ic_error_cloud_off_24dp)
        }
        // TODO: consider showing "retry" button
        feedRecyclerView.visibility = View.INVISIBLE
        emptyView.apply {
            setTitle(triple.first)
            setCaption(triple.second)
            setImageDrawable(triple.third)
            visibility = View.VISIBLE
        }
    }
}
