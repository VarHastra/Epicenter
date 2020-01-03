package com.github.varhastra.epicenter.presentation.common.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.varhastra.epicenter.R

class TileView : FrameLayout {

    lateinit var titleTextView: TextView
    lateinit var textTextView: TextView
    lateinit var iconImageView: ImageView

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.view_tile, this)

        titleTextView = findViewById(R.id.tv_tile_title)
        textTextView = findViewById(R.id.tv_tile_text)
        iconImageView = findViewById(R.id.iv_tile_icon)

        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.TileView)
        styleAttrs.apply {
            val title = getString(R.styleable.TileView_title)
            val caption = getString(R.styleable.TileView_text)
            val drawable = getDrawable(R.styleable.TileView_icon)

            setTitle(title ?: "")
            setText(caption ?: "")
            setIconDrawable(drawable)
        }

        styleAttrs.recycle()
    }

    fun setTitle(@StringRes stringResId: Int) {
        titleTextView.setText(stringResId)
    }

    fun setTitle(text: String) {
        titleTextView.text = text
    }

    fun setText(@StringRes stringRes: Int) {
        textTextView.setText(stringRes)
    }

    fun setText(text: String) {
        textTextView.text = text
    }

    fun setIconDrawable(@DrawableRes drawableRes: Int) {
        iconImageView.setImageResource(drawableRes)
    }

    fun setIconDrawable(drawable: Drawable?) {
        iconImageView.setImageDrawable(drawable)
    }
}