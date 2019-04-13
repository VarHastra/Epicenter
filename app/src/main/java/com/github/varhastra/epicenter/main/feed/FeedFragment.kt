package com.github.varhastra.epicenter.main.feed


import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife

import com.github.varhastra.epicenter.R
import com.github.varhastra.epicenter.views.EmptyView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * A [Fragment] subclass that displays a list
 * of recent earthquakes.
 */
class FeedFragment : Fragment() {

    @BindView(R.id.cg_feed_filters_magnitude)
    lateinit var magnitudeChipGroup: ChipGroup

    @BindView(R.id.cg_feed_filters_sort_by)
    lateinit var sortingChipGroup: ChipGroup

    @BindView(R.id.sheet_feed)
    lateinit var sheetFeed: ViewGroup
    lateinit var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>

    @BindView(R.id.emptv_feed)
    lateinit var emptyView: EmptyView



    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_feed, container, false)
        ButterKnife.bind(this, root)

        setHasOptionsMenu(true)

        magnitudeChipGroup.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach {
                (it as? Chip)?.apply {
                    isClickable = !isChecked
                }
            }
        }

        sortingChipGroup.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach {
                (it as? Chip)?.apply {
                    isClickable = !isChecked
                }
            }
        }

        bottomSheetBehavior = BottomSheetBehavior.from(sheetFeed)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        return root
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
}
