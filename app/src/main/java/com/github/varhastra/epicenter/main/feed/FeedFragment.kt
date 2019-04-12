package com.github.varhastra.epicenter.main.feed


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife

import com.github.varhastra.epicenter.R
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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_feed, container, false)
        ButterKnife.bind(this, root)

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

        return root
    }


}
