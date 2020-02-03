package com.github.varhastra.epicenter.common.extensions

import android.widget.SeekBar

fun SeekBar.onStopTrackingTouch(f: (SeekBar) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            // Intentionally do nothing
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            // Intentionally do nothing
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            f(seekBar)
        }
    })
}