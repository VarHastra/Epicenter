package com.github.varhastra.epicenter.ui.main.feed


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
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.children
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.data.Prefs
import com.github.varhastra.epicenter.domain.model.FeedFilter
import com.github.varhastra.epicenter.domain.model.Place
import com.github.varhastra.epicenter.domain.model.RemoteEvent
import com.github.varhastra.epicenter.ui.details.DetailsActivity
import com.github.varhastra.epicenter.ui.main.ToolbarProvider
import com.github.varhastra.epicenter.ui.placesmanager.PlacesManagerActivity
import com.github.varhastra.epicenter.views.EmptyView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.startActivity
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

        feedAdapter = FeedAdapter(activity!!, Prefs.getPreferredUnits())
        feedAdapter.setHasStableIds(true)
        feedAdapter.onEventClickListener = { remoteEvent, _ ->
            presenter.openEventDetails(remoteEvent.event.id)
        }

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
        toolbarProvider?.attachOnEditListener {
            presenter.openPlacesEditor()
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
                R.id.chip_sorting_mag_asc -> FeedFilter.Sorting.MAGNITUDE_ASC
                R.id.chip_sorting_mag_desc -> FeedFilter.Sorting.MAGNITUDE_DESC
                R.id.chip_sorting_distance -> FeedFilter.Sorting.DISTANCE
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
        activity?.apply {
            this as ToolbarProvider
            showDropdown(true)
        }
    }

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
            FeedFilter.Sorting.MAGNITUDE_ASC -> R.id.chip_sorting_mag_asc
            FeedFilter.Sorting.MAGNITUDE_DESC -> R.id.chip_sorting_mag_desc
            FeedFilter.Sorting.DISTANCE -> R.id.chip_sorting_distance
        }
        sortingChipGroup.check(id)
    }

    override fun showPlaces(places: List<Place>) {
        toolbarProvider?.setDropdownData(places)
    }

    override fun showEvents(events: List<RemoteEvent>) {
        emptyView.visibility = View.INVISIBLE
        feedRecyclerView.visibility = View.VISIBLE
        feedAdapter.data = events
        feedRecyclerView.scrollToPosition(0)
        feedRecyclerView.scheduleLayoutAnimation()
    }

    override fun showErrorNoData(reason: FeedContract.View.ErrorReason) {
        val triple = when (reason) {
            FeedContract.View.ErrorReason.ERR_NO_EVENTS -> Triple(
                    R.string.app_error_no_events,
                    R.string.app_error_no_events_capt,
                    R.drawable.ic_error_earth_24px
            )
            FeedContract.View.ErrorReason.ERR_NO_CONNECTION -> Triple(
                    R.string.app_error_no_connection,
                    R.string.app_error_no_connection_capt,
                    R.drawable.ic_error_wifi_off_24px
            )
            FeedContract.View.ErrorReason.ERR_UNKNOWN -> Triple(
                    R.string.app_error_unknown,
                    R.string.app_error_unknown_capt,
                    R.drawable.ic_error_cloud_off_24dp
            )
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
        view?.longSnackbar(R.string.feed_error_location_not_avaliable, R.string.app_settings) {
            showAppSettings()
        }
    }

    override fun showErrorNoConnection() {
        view?.longSnackbar(R.string.app_error_no_connection)
    }

    override fun showPlacesEditor() {
        activity?.startActivity<PlacesManagerActivity>()
    }

    override fun showEventDetails(eventId: String) {
        val hostActivity = activity
        hostActivity?.let {
            val intent = Intent(hostActivity, DetailsActivity::class.java)
            intent.putExtra(DetailsActivity.EXTRA_EVENT_ID, eventId)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(hostActivity).toBundle()
            startActivityForResult(intent, REQUEST_DETAILS, options)
        }
    }

    private fun showAppSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context?.packageName, null)
        }
        startActivity(intent)
    }


    companion object {
        private const val REQUEST_DETAILS = 100
    }
}
