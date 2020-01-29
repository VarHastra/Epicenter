package com.github.varhastra.epicenter.common.extensions

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar


fun View.snackbar(@StringRes resId: Int, @StringRes actionResId: Int, action: (View) -> Unit): Snackbar {
    return Snackbar.make(this, resId, Snackbar.LENGTH_SHORT)
            .setAction(actionResId, action)
            .apply { show() }
}

fun View.longSnackbar(@StringRes resId: Int, @StringRes actionResId: Int, action: (View) -> Unit): Snackbar {
    return Snackbar.make(this, resId, Snackbar.LENGTH_LONG)
            .setAction(actionResId, action)
            .apply { show() }
}

fun View.snackbar(@StringRes resId: Int): Snackbar {
    return Snackbar.make(this, resId, Snackbar.LENGTH_SHORT)
            .apply { show() }
}

fun View.longSnackbar(@StringRes resId: Int): Snackbar {
    return Snackbar.make(this, resId, Snackbar.LENGTH_LONG)
            .apply { show() }
}