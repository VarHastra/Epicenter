package com.github.varhastra.epicenter.common.extensions

import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

fun ChipGroup.setRestrictiveCheckListener(onCheck: (ChipGroup, Int) -> Unit) = setOnCheckedChangeListener { group, checkedId ->
    group.chips.forEach { chip ->
        chip.isClickable = chip.id != checkedId
    }
    onCheck(group, checkedId)
}

val ChipGroup.chips: Sequence<Chip>
    get() = children.mapNotNull { it as? Chip }