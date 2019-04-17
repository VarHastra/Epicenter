package com.github.varhastra.epicenter.views

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
import com.google.android.material.button.MaterialButton

class EmptyView : FrameLayout {

    private lateinit var titleTextView: TextView
    private lateinit var captionTextView: TextView
    private lateinit var imageView: ImageView
    private lateinit var button: MaterialButton

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        View.inflate(context, R.layout.empty_view, this)

        titleTextView = findViewById(R.id.tv_empty_view_title)
        captionTextView = findViewById(R.id.tv_empty_view_caption)
        imageView = findViewById(R.id.iv_empty_view)
        button = findViewById(R.id.btn_empty_view);

        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.EmptyView)
        styleAttrs.apply {
            val title = getString(R.styleable.EmptyView_text)
            val caption = getString(R.styleable.EmptyView_captionText)
            val drawable = getDrawable(R.styleable.EmptyView_image)
            val buttonText = getString(R.styleable.EmptyView_buttonText)
            val buttonVisibility = getBoolean(R.styleable.EmptyView_buttonVisibility, false)

            setTitle(title ?: "")
            setCaption(caption ?: "")
            setImageDrawable(drawable)
            setButtonText(buttonText ?: "")
            setButtonVisibility(buttonVisibility)
        }

        styleAttrs.recycle()
    }

    fun setTitle(@StringRes stringResId: Int) {
        titleTextView.setText(stringResId)
    }

    fun setTitle(text: String) {
        titleTextView.text = text
    }

    fun setCaption(@StringRes stringRes: Int) {
        captionTextView.setText(stringRes)
    }

    fun setCaption(text: String) {
        captionTextView.text = text
    }

    fun setImageDrawable(@DrawableRes drawableRes: Int) {
        imageView.setImageResource(drawableRes)
    }

    fun setImageDrawable(drawable: Drawable?) {
        imageView.setImageDrawable(drawable)
    }

    fun setButtonText(text: String) {
        button.text = text
    }

    fun setButtonText(@StringRes stringRes: Int) {
        button.setText(stringRes)
    }

    fun setButtonListener(listener: (View) -> Unit) {
        button.setOnClickListener(listener)
    }

    fun setButtonVisibility(visible: Boolean) {
        button.visibility = if (visible) View.VISIBLE else View.GONE
    }
}