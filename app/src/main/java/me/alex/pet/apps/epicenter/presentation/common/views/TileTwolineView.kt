package me.alex.pet.apps.epicenter.presentation.common.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import me.alex.pet.apps.epicenter.R

class TileTwolineView : FrameLayout {

    lateinit var titleTextView: TextView
    lateinit var firstLineTextTextView: TextView
    lateinit var secondLineTextTextView: TextView
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
        View.inflate(context, R.layout.view_tile_twoline, this)

        titleTextView = findViewById(R.id.tv_tile_title)
        firstLineTextTextView = findViewById(R.id.tv_tile_text_first)
        secondLineTextTextView = findViewById(R.id.tv_tile_text_second)
        iconImageView = findViewById(R.id.iv_tile_icon)

        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.TileTwolineView)
        styleAttrs.apply {
            val title = getString(R.styleable.TileTwolineView_title)
            val caption1 = getString(R.styleable.TileTwolineView_textFirstLine)
            val caption2 = getString(R.styleable.TileTwolineView_textSecondLine)
            val drawable = getDrawable(R.styleable.TileTwolineView_icon)

            setTitle(title ?: "")
            setFirstLineText(caption1 ?: "")
            setSecondLineText(caption2 ?: "")
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

    fun setFirstLineText(@StringRes stringRes: Int) {
        firstLineTextTextView.setText(stringRes)
    }

    fun setFirstLineText(text: String) {
        firstLineTextTextView.text = text
    }

    fun setSecondLineText(@StringRes stringRes: Int) {
        secondLineTextTextView.setText(stringRes)
    }

    fun setSecondLineText(text: String) {
        secondLineTextTextView.text = text
    }

    fun setIconDrawable(@DrawableRes drawableRes: Int) {
        iconImageView.setImageResource(drawableRes)
    }

    fun setIconDrawable(drawable: Drawable?) {
        iconImageView.setImageDrawable(drawable)
    }
}