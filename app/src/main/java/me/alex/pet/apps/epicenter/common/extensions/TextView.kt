package me.alex.pet.apps.epicenter.common.extensions

import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun TextView.setTextColorRes(@ColorRes resId: Int) = setTextColor(ContextCompat.getColor(this.context, resId))
